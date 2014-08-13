package edu.tamu.tcat.sda.catalog.psql.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.impl.WorkImpl;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class PsqlGetWorkTask implements DbExecTask<Work>
{
   private final static String sql = "SELECT work FROM works WHERE id=?";

   private final JsonMapper jsonMapper;
   private final int workId;

   PsqlGetWorkTask(int id, JsonMapper jsonMapper)
   {
      this.jsonMapper = jsonMapper;
      this.workId = id;
   }

   @Override
   public Work execute(Connection conn)// throws Exception
   {
      try (PreparedStatement ps = conn.prepareStatement(sql))
      {
         ps.setLong(1, workId);
         ResultSet rs = ps.executeQuery();
         if(rs.next())
         {
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
         return null;
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Failed to list bibliographic entry", e);
      }
   }
}
