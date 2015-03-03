package edu.tamu.tcat.catalogentries.bibliography.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.catalogentries.bibliography.Work;
import edu.tamu.tcat.catalogentries.bibliography.dv.WorkDV;
import edu.tamu.tcat.catalogentries.bibliography.postgres.model.WorkImpl;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;

public final class PsqlListWorksTask implements SqlExecutor.ExecutorTask<Iterable<Work>>
{

   private final static String sql = "SELECT work FROM works";

   private final JsonMapper jsonMapper;

   PsqlListWorksTask(JsonMapper jsonMapper)
   {
      this.jsonMapper = jsonMapper;
   }

   @Override
   public Iterable<Work> execute(Connection conn)// throws Exception
   {
      List<Work> works = new ArrayList<>();
      try (PreparedStatement ps = conn.prepareStatement(sql);
           ResultSet rs = ps.executeQuery())
      {
         while(rs.next())
         {
            PGobject pgo = (PGobject)rs.getObject("work");
            String workJson = pgo.toString();
            try
            {
               WorkDV dv = jsonMapper.parse(workJson, WorkDV.class);
               works.add(new WorkImpl(dv));
            }
            catch (JsonException e)
            {
               throw new IllegalStateException("Failed to parse bibliographic record\n" + workJson, e);
            }
         }

         return works;
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Failed to list bibliographic entries", e);
      }
   }
}