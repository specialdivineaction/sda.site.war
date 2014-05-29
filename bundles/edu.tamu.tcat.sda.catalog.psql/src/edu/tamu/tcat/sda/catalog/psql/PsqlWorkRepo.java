package edu.tamu.tcat.sda.catalog.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.db.ExecutionFailedException;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.impl.WorkImpl;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class PsqlWorkRepo implements WorkRepository
{
   private DbExecutor exec;
   private JsonMapper jsonMapper;

   public PsqlWorkRepo()
   {
   }
   
   public void setDatabaseExecutor(DbExecutor exec)
   {
      this.exec = exec;
   }
   
   public void setJsonMapper(JsonMapper mapper)
   {
      this.jsonMapper = mapper;
   }
   
   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(jsonMapper);
   }
   
   public void dispose()
   {
      this.exec = null;
      this.jsonMapper = null;
   }

   @Override
   public Iterable<Work> listWorks()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void create(final WorkDV work, DataUpdateObserver<Work> observer)
   {
      final String workString;
      try
      {
         workString = jsonMapper.asString(work);
      }
      catch (JsonException jpe)
      {
         throw new IllegalArgumentException("Failed to serialize the supplied work [" + work + "]", jpe);
      }

      final String sql = "INSERT INTO works (work) VALUES(?)";
      DbExecTask<Work> task = new DbExecTask<Work>()
      {
         @Override
         public Work execute(Connection conn) throws SQLException, ExecutionFailedException
         {
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
            {
               PGobject jsonObject = new PGobject();
               jsonObject.setType("json");
               jsonObject.setValue(workString);

               ps.setObject(1, jsonObject);

               int ct = ps.executeUpdate();
               if (ct != 1)
                  throw new ExecutionFailedException("Failed to create work. Unexpected number of rows updates [" + ct + "]");

//               if (ps.isClosed())
//               {
                  ResultSet rs = ps.getGeneratedKeys();
                  if (!rs.next())
                     throw new ExecutionFailedException("Failed to generate id for a work [" + work + "]");
                  work.id = Integer.toString(rs.getInt("id"));
//               }
               return new WorkImpl(work);
            }
            catch (SQLException e)
            {
               throw new IllegalStateException("Failed to create work: [" + work + "]");
            }
         }
      };
      
      exec.submit(new ObservableTaskWrapper<>(task, observer));
   }

   @Override
   public void update(WorkDV work, DataUpdateObserver<Work> observer)
   {
      // TODO Auto-generated method stub

   }

}
