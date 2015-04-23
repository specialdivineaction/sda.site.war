package edu.tamu.tcat.trc.entries.bib.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;

@Deprecated // no need to provide separate impl
public class PsqlGetWorkTask implements SqlExecutor.ExecutorTask<Work>
{
   private final static String sql = "SELECT work FROM works WHERE id=?";

   private final ObjectMapper jsonMapper;
   private final String workId;

   PsqlGetWorkTask(String id, ObjectMapper jsonMapper)
   {
      this.jsonMapper = jsonMapper;
      this.workId = id;
   }

   @Override
   public Work execute(Connection conn) throws NoSuchCatalogRecordException
   {
      try (PreparedStatement ps = conn.prepareStatement(sql))
      {
         ps.setString(1, workId);
         try (ResultSet rs = ps.executeQuery())
         {
            if (!rs.next())
               throw new NoSuchCatalogRecordException("No catalog record exists for work id=" + workId);

            PGobject pgo = (PGobject)rs.getObject("work");
            String workJson = pgo.toString();
            try
            {
               WorkDV dv = jsonMapper.readValue(workJson, WorkDV.class);
               return WorkDV.instantiate(dv);
            }
            catch (IOException e)
            {
               throw new IllegalStateException("Failed to parse bibliographic record\n" + workJson, e);
            }
         }
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Failed to retrieve bibliographic entry [entry id = " + workId + "]", e);
      }
   }
}
