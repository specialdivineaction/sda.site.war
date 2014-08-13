package edu.tamu.tcat.sda.catalog.psql.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.db.ExecutionFailedException;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.impl.WorkImpl;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public final class PsqlCreateWorkTask implements DbExecTask<Work>
{
   private final static String insertSql = "INSERT INTO works (work) VALUES(?)";
   private final static String updateSql = "UPDATE works "
                                         + "   SET work = ?"
                                         + "   WHERE id = ?";

   private final WorkDV work;
   private final JsonMapper jsonMapper;

   PsqlCreateWorkTask(WorkDV work, JsonMapper jsonMapper)
   {
      // TODO convert to form where these can be configured using plugins/task provider, etc.
      this.work = work;
      this.jsonMapper = jsonMapper;
   }

   private String getJson()
   {
      try
      {
         return jsonMapper.asString(work);
      }
      catch (JsonException jpe)
      {
         throw new IllegalArgumentException("Failed to serialize the supplied work [" + work + "]", jpe);
      }
   }

   @Override
   public Work execute(Connection conn) throws SQLException, ExecutionFailedException
   {
      try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS))
      {
         PGobject jsonObject = new PGobject();
         jsonObject.setType("json");
         jsonObject.setValue(getJson());

         ps.setObject(1, jsonObject);

         int ct = ps.executeUpdate();
         if (ct != 1)
            throw new ExecutionFailedException("Failed to create work. Unexpected number of rows updates [" + ct + "]");

         try (ResultSet rs = ps.getGeneratedKeys())
         {
            if (!rs.next())
               throw new ExecutionFailedException("Failed to generate id for a work [" + work + "]");

            work.id = Integer.toString(rs.getInt("id"));
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql))
            {
               PGobject updatedJsonObject = new PGobject();
               updatedJsonObject.setType("json");
               updatedJsonObject.setValue(getJson());

               updatePs.setObject(1, updatedJsonObject);
               updatePs.setInt(2, Integer.parseInt(work.id));

               int updateCt = updatePs.executeUpdate();
               if (updateCt != 1)
                  throw new ExecutionFailedException("Failed to create work. Unexpected number of rows updates [" + updateCt + "]");
            }
            catch (SQLException ue)
            {
               throw new IllegalStateException("Failed to create work: [" + work + "]");
            }

         }

         return new WorkImpl(work);
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Failed to create work: [" + work + "]");
      }
   }
}