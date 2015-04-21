package edu.tamu.tcat.trc.entries.bib.copy.ref.postgres;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.CopyRefDTO;
import edu.tamu.tcat.trc.entries.bib.CopyReference;
import edu.tamu.tcat.trc.entries.bib.CopyReferenceException;
import edu.tamu.tcat.trc.entries.bib.CopyReferenceRepository;
import edu.tamu.tcat.trc.entries.bib.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.persist.BasicUpdateEvent;
import edu.tamu.tcat.trc.persist.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.persist.EntryUpdateHelper;
import edu.tamu.tcat.trc.persist.ObservableTaskWrapper;
import edu.tamu.tcat.trc.persist.UpdateEvent;
import edu.tamu.tcat.trc.persist.UpdateEvent.UpdateAction;
import edu.tamu.tcat.trc.persist.UpdateListener;

public class PsqlDigitalCopyLinkRepo implements CopyReferenceRepository
{
   private static final Logger logger = Logger.getLogger(PsqlDigitalCopyLinkRepo.class.getName());

   private static final String GET_SQL =
         "SELECT reference "
        +  "FROM copy_references "
        + "WHERE ref_id = ? AND active = true";
   private static final String GET_ANY_SQL =
         "SELECT reference "
        +  "FROM copy_references "
        + "WHERE ref_id = ?";
   private static final String REMOVE_SQL =
         "UPDATE copy_references "
         + " SET active = false, "
         +     " date_modified = now() "
         +"WHERE ref_id = ?";


   private SqlExecutor exec;

   private EntryUpdateHelper<CopyReference> listeners;

   private ObjectMapper mapper;

   public PsqlDigitalCopyLinkRepo()
   {
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void activate()
   {
      listeners = new EntryUpdateHelper<>();

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public void dispose()
   {
      this.exec = null;

      if (listeners != null)
         listeners.close();

      listeners = null;
      mapper = null;
   }

   @Override
   public EditCopyReferenceCommand create()
   {
      return new EditCopyRefCmdImpl(exec, listeners, new UpdateEventFactory());
   }

   @Override
   public EditCopyReferenceCommand edit(UUID id) throws NoSuchCatalogRecordException
   {
      CopyRefDTO dto = getCopyDTO(GET_SQL, id);
      return new EditCopyRefCmdImpl(exec, listeners, new UpdateEventFactory(), dto);
   }

   @Override
   public Set<CopyReference> getCopies(URI entity)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public CopyReference get(UUID id) throws NoSuchCatalogRecordException
   {
      return CopyRefDTO.instantiate(getCopyDTO(GET_SQL, id));
   }

   @Override
   public Future<Boolean> remove(UUID id) throws CopyReferenceException
   {
      UpdateEventFactory factory = new UpdateEventFactory();
      UpdateEvent<CopyReference> evt = factory.makeDeleteEvent(id);

      boolean shouldExecute = listeners.before(evt);

      return exec.submit(new ObservableTaskWrapper<Boolean>(
            makeRemoveTask(id, shouldExecute),
            new DataUpdateObserverAdapter<Boolean>()
            {
               @Override
               protected void onFinish(Boolean result) {
                  if (result.booleanValue())
                     listeners.after(evt);
               }
            }));
   }

   private SqlExecutor.ExecutorTask<Boolean> makeRemoveTask(UUID id, boolean shouldExecute)
   {
      return (conn) -> {
         if (!shouldExecute)
            return Boolean.valueOf(false);

         try (PreparedStatement ps = conn.prepareStatement(REMOVE_SQL))
         {
            ps.setString(1, id.toString());
            int ct = ps.executeUpdate();
            if (ct == 0)
            {
               logger.log(Level.WARNING, "Failed to remove copy reference [" + id + "]. Reference may not exist.", id);
               return Boolean.valueOf(false);
            }

            return Boolean.valueOf(true);
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to remove copy reference [" + id + "]. ", e);
         }
      };
   }

   public class UpdateEventFactory
   {
      public UpdateEvent<CopyReference> create(CopyReference newRef)
      {
         return new BasicUpdateEvent<>(newRef.getId().toString(),
                                       UpdateAction.CREATE,
                                       () -> null,
                                       () -> newRef);
      }

      public UpdateEvent<CopyReference> edit(CopyReference orig, CopyReference updated)
      {
         return new BasicUpdateEvent<>(updated.getId().toString(),
               UpdateAction.UPDATE,
               () -> orig,
               () -> updated);
      }

      public UpdateEvent<CopyReference> makeDeleteEvent(UUID id)
      {
         return new BasicUpdateEvent<>(id.toString(),
               UpdateAction.DELETE,
               () -> {
                  try {
                     return CopyRefDTO.instantiate(getCopyDTO(GET_ANY_SQL, id));
                  } catch (Exception ex) {
                     return null;
                  }
               },
               () -> null);
      }
   }

   private CopyRefDTO executeGetQuery(String sql, Connection conn, UUID id) throws NoSuchCatalogRecordException
   {
      try (PreparedStatement ps = conn.prepareStatement(sql))
      {
         ps.setString(1, id.toString());
         try (ResultSet rs = ps.executeQuery())
         {
            if (!rs.next())
               throw new NoSuchCatalogRecordException("No catalog record exists for work id=" + id);

            PGobject pgo = (PGobject)rs.getObject("reference");
            return parseCopyRefJson(pgo.toString());
         }
      }
      catch(SQLException e)
      {
         throw new IllegalStateException("Failed to retrive copy reference [" + id + "]. ", e);
      }
   }

   private CopyRefDTO parseCopyRefJson(String json)
   {
      try
      {
         return mapper.readValue(json, CopyRefDTO.class);
      }
      catch (IOException e)
      {
         throw new IllegalStateException("Failed to parse relationship record\n" + json, e);
      }
   }

   private CopyRefDTO getCopyDTO(String sql, UUID id) throws NoSuchCatalogRecordException
   {
      Future<CopyRefDTO> result = exec.submit((conn) -> executeGetQuery(sql, conn, id));

      try
      {
         return result.get();
      }
      catch (InterruptedException e)
      {
         throw new IllegalStateException("Failed to retrieve copy reference [" + id + "].", e);
      }
      catch (ExecutionException e)
      {
         // unwrap the execution exception that may be thrown from the executor
         Throwable cause = e.getCause();
         if (cause instanceof NoSuchCatalogRecordException)
            throw (NoSuchCatalogRecordException)cause;         // if not found
         else if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;                     // 'expected' internal errors - json parsing, db access, etc
         else if (cause instanceof Error)
            throw (Error)cause;                                // OoM and other system errors
         else                                                  // unanticipated errors
            throw new IllegalStateException("Unknown error while attempting to retrive copy reference [" + id + "]", cause);
      }
   }

   @Override
   public AutoCloseable register(UpdateListener<CopyReference> ears)
   {
      Objects.requireNonNull(listeners, "Update registration is not available at this time.");
      return listeners.register(ears);
   }
}
