package edu.tamu.tcat.sda.catalog.psql.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.impl.WorkImpl;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public final class PsqlListWorksTask implements DbExecTask<Iterable<Work>>
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