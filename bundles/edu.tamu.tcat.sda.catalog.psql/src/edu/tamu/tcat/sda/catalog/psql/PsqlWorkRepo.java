package edu.tamu.tcat.sda.catalog.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.ds.DataUpdateObserver;

public class PsqlWorkRepo implements WorkRepository
{
   // TODO should we use something like the data source executor service?

   private final DbExecutor exec;
   private final JsonMapper jsonMapper;

   public PsqlWorkRepo(DbExecutor exec, JsonMapper jsonMapper)
   {
      this.exec = exec;
      this.jsonMapper = jsonMapper;
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

      final String sql = "INSERT INTO works (id, work) VALUES(?,?)";
      DbExecTask<Work> task = new DbExecTask<Work>()
      {
         @Override
         public Work execute(Connection conn) throws SQLException
         {
            try (PreparedStatement ps = conn.prepareStatement(sql))
            {
               PGobject jsonObject = new PGobject();
               jsonObject.setType("json");
               jsonObject.setValue(workString);

               ps.setString(1, work.id);
               ps.setObject(2, jsonObject);

               int ct = ps.executeUpdate();
               if (ct != 1)
                  throw new IllegalStateException("Failed to create work. Unexpected number of rows updates [" + ct + "]");

               return new WorkRef(Long.parseLong(work.id));
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

   private static class WorkRef implements Work
   {
      public WorkRef(long id)
      {
         // TODO implement this. Capture the id of the work, use it to query the DB as 
         //      needed to lazy load data.
      }

      @Override
      public List<AuthorReference> getAuthors()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Title getTitle()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public String getSeries()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public String getSummary()
      {
         // TODO Auto-generated method stub
         return null;
      }
   }

//   private Connection getPostgresConn()
//   {
//      Connection con = null;
//
//      String url = "jdbc:postgresql://localhost:5433/SDA";
//      String user = "postgres";
//      String password = "";
//
//      try
//      {
//         Class.forName("org.postgresql.Driver");
//         con = DriverManager.getConnection(url, user, password);
//      }
//      catch (SQLException | ClassNotFoundException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//      return con;
//   }

}
