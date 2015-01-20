package edu.tamu.tcat.sda.catalog.relationships.psql;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.IdFactory;
import edu.tamu.tcat.sda.catalog.psql.ObservableTaskWrapper;
import edu.tamu.tcat.sda.catalog.psql.impl.EditRelationshipCommandImpl;
import edu.tamu.tcat.sda.catalog.relationship.EditRelationshipCommand;
import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipChangeEvent;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipChangeEvent.ChangeType;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipNotAvailableException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipPersistenceException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipTypeRegistry;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class PsqlRelationshipRepo implements RelationshipRepository
{

   public PsqlRelationshipRepo()
   {
   }

   private static final String ID_CONTEXT = "relationships";
   private SqlExecutor exec;
   private IdFactory idFactory;
   private JsonMapper jsonMapper;
   private RelationshipTypeRegistry typeReg;

   private final CopyOnWriteArrayList<Consumer<RelationshipChangeEvent>> listeners = new CopyOnWriteArrayList<>();


   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setJsonMapper(JsonMapper mapper)
   {
      this.jsonMapper = mapper;
   }

   public void setIdFactory(IdFactory factory)
   {
      this.idFactory = factory;
   }

   public void setTypeRegistry(RelationshipTypeRegistry typeReg)
   {
      this.typeReg = typeReg;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(jsonMapper);
      Objects.requireNonNull(idFactory);
   }

   public void dispose()
   {
      this.exec = null;
      this.jsonMapper = null;
      this.idFactory = null;

      listeners.clear();
   }

   @Override
   public Relationship get(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      PsqlGetRelationshipTask task = new PsqlGetRelationshipTask(id, jsonMapper, typeReg);
      try
      {
         return exec.submit(task).get();
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof RelationshipNotAvailableException)
            throw (RelationshipNotAvailableException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve relationship entry [" + id +"]" , e);
      }
      catch (InterruptedException e) {
         throw new IllegalStateException("Failed to retrieve relationship entry [" + id +"]", e);
      }
   }

   @Override
   public EditRelationshipCommand create() throws RelationshipPersistenceException
   {
      RelationshipDV relationship = new RelationshipDV();
      relationship.id = idFactory.getNextId(ID_CONTEXT);

      EditRelationshipCommandImpl command = new EditRelationshipCommandImpl(relationship, idFactory);
      command.setCommitHook((r) -> {
         PsqlCreateRelationshipTask task = new PsqlCreateRelationshipTask(r, jsonMapper);

         WorkChangeNotifier<String> workChangeNotifier = new WorkChangeNotifier<>(r.id, ChangeType.CREATED);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(task, workChangeNotifier);

         Future<String> future = exec.submit(wrappedTask);
         return future;
      });
      return command;
   }

   @Override
   public EditRelationshipCommand edit(final String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      EditRelationshipCommandImpl command = new EditRelationshipCommandImpl(RelationshipDV.create(get(id)) , idFactory);
      command.setCommitHook((r) -> {
         PsqlUpdateRelationshipTask task = new PsqlUpdateRelationshipTask(r, jsonMapper);

         WorkChangeNotifier<String> workChangeNotifier = new WorkChangeNotifier<>(id, ChangeType.MODIFIED);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(task, workChangeNotifier);

         Future<String> future = exec.submit(wrappedTask);
         return future;
      });
      return command;
   }

   @Override
   public void delete(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      PsqlDeleteRelationshipTask deleteTask = new PsqlDeleteRelationshipTask(id);
      WorkChangeNotifier<Void> workChangeNotifier = new WorkChangeNotifier<>(id, ChangeType.DELETED);
      ObservableTaskWrapper<Void> wrappedTask = new ObservableTaskWrapper<>(deleteTask, workChangeNotifier);

      exec.submit(wrappedTask);
   }

   private void notifyRelationshipUpdate(ChangeType type, String relnId)
   {
      RelationshipChangeEventImpl evt = new RelationshipChangeEventImpl(type, relnId);
      listeners.forEach(ears -> ears.accept(evt));
   }

   @Override
   public AutoCloseable addUpdateListener(Consumer<RelationshipChangeEvent> ears)
   {
      listeners.add(ears);
      return () -> listeners.remove(ears);
   }

   private final class WorkChangeNotifier<ResultType> implements DataUpdateObserver<ResultType>
   {
      private final String id;
      private final ChangeType type;

      public WorkChangeNotifier(String id, ChangeType type)
      {
         this.id = id;
         this.type = type;

      }

      @Override
      public boolean start()
      {
         return true;
      }

      @Override
      public void finish(ResultType result)
      {
         notifyRelationshipUpdate(type, id);
      }

      @Override
      public void aborted()
      {
         // no-op
      }

      @Override
      public void error(String message, Exception ex)
      {
         // no-op
      }

      @Override
      public boolean isCanceled()
      {
         return false;
      }

      @Override
      public boolean isCompleted()
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public State getState()
      {
         throw new UnsupportedOperationException();
      }
   }

   private class RelationshipChangeEventImpl implements RelationshipChangeEvent
   {
      private final ChangeType type;
      private final String id;

      public RelationshipChangeEventImpl(ChangeType type, String id)
      {
         this.type = type;
         this.id = id;
      }

      @Override
      public ChangeType getChangeType()
      {
         return type;
      }

      @Override
      public String getRelationshipId()
      {
         return id;
      }

      @Override
      public Relationship getRelationship() throws RelationshipNotAvailableException
      {
         try
         {
            return get(id);
         }
         catch (RelationshipPersistenceException e)
         {
            throw new RelationshipNotAvailableException("Internal error failed to retrieve relationship [" + id + "].", e);
         }
      }

      @Override
      public String toString()
      {
         return "Relationship Change Event: action = " + type + "; id = " + id;
      }
   }

}
