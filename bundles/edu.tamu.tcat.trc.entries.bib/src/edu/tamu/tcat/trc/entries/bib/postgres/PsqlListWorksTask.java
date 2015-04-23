package edu.tamu.tcat.trc.entries.bib.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;

public final class PsqlListWorksTask implements SqlExecutor.ExecutorTask<Iterable<Work>>
{

   private final static String sql = "SELECT work FROM works WHERE active = true";

   private final ObjectMapper jsonMapper;

   PsqlListWorksTask(ObjectMapper jsonMapper)
   {
      this.jsonMapper = jsonMapper;
   }

   @Override
   public Iterable<Work> execute(Connection conn) // throws Exception
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
               works.add(WorkDV.instantiate(dv));
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