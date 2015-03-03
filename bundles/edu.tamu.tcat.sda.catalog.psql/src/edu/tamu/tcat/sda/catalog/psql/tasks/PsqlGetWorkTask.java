package edu.tamu.tcat.sda.catalog.psql.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.catalogentries.bibliography.Work;
import edu.tamu.tcat.catalogentries.bibliography.dv.WorkDV;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.impl.WorkImpl;

public class PsqlGetWorkTask implements SqlExecutor.ExecutorTask<Work>
{
   private final static String sql = "SELECT work FROM works WHERE id=?";

   private final JsonMapper jsonMapper;
   private final String workId;

   PsqlGetWorkTask(String id, JsonMapper jsonMapper)
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
               WorkDV dv = jsonMapper.parse(workJson, WorkDV.class);
               return new WorkImpl(dv);
            }
            catch (JsonException e)
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
