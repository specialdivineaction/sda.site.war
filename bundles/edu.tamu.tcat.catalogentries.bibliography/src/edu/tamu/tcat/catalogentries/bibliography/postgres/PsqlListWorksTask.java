package edu.tamu.tcat.catalogentries.bibliography.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.catalogentries.bibliography.Work;
import edu.tamu.tcat.catalogentries.bibliography.dv.WorkDV;
import edu.tamu.tcat.catalogentries.bibliography.postgres.model.WorkImpl;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public final class PsqlListWorksTask implements SqlExecutor.ExecutorTask<Iterable<Work>>
{

   private final static String sql = "SELECT work FROM works";

   private final ObjectMapper jsonMapper;

   PsqlListWorksTask(ObjectMapper jsonMapper)
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
               WorkDV dv = jsonMapper.readValue(workJson, WorkDV.class);
               works.add(new WorkImpl(dv));
            }
            catch (IOException e)
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