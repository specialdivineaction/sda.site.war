package edu.tamu.tcat.catalogentries.bibliography.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.catalogentries.bibliography.dv.WorkDV;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public class PsqlUpdateWorksTask implements SqlExecutor.ExecutorTask<String>
{
   private final static Logger DbTaskLogger = Logger.getLogger("edu.tamu.tcat.catalogentries.bibliography.db.errors");
   private final static String sql = "Update works "
                            + "   SET work = ?"
                            + "   WHERE id = ?";

   private final ObjectMapper jsonMapper;
   private final WorkDV work;

   public PsqlUpdateWorksTask(WorkDV work, ObjectMapper jsonMapper)
   {
      this.work = work;
      this.jsonMapper = jsonMapper;
   }

   private String getJson()
   {
      try
      {
         return jsonMapper.writeValueAsString(work);
      }
      catch (IOException je)
      {
         throw new IllegalArgumentException("Failed to serialize the supplied work [" + work + "]", je);
      }
   }

   @Override
   public String execute(Connection conn) throws Exception
   {
      try (PreparedStatement ps = conn.prepareStatement(sql))
      {
         PGobject jsonObject = new PGobject();
         jsonObject.setType("json");
         jsonObject.setValue(getJson());

         ps.setObject(1, jsonObject);
         ps.setString(2, work.id);

         int ct = ps.executeUpdate();
         if (ct != 1)
            throw new ExecutionFailedException("Failed to create work. Unexpected number of rows updates [" + ct + "]");


         return work.id;
      }
      catch(SQLException e)
      {
         throw new IllegalStateException("Failed to create work: [" + work + "]");
      }
   }
}
