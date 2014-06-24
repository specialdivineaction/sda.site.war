package edu.tamu.tcat.sda.catalog.psql.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.oss.db.DbExecTask;
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
   public Iterable<Work> execute(Connection conn) throws Exception
   {
      List<Work> events = new ArrayList<>();
      Iterable<Work> eIterable = new ArrayList<>();
      try (PreparedStatement ps = conn.prepareStatement(sql);
           ResultSet rs = ps.executeQuery())
      {
         PGobject pgo = new PGobject();

         while(rs.next())
         {
            Object object = rs.getObject("work");
            if (object instanceof PGobject)
               pgo = (PGobject)object;
            else
               System.out.println("Error!");

            WorkDV parse = jsonMapper.parse(pgo.toString(), WorkDV.class);
            WorkImpl figureRef = new WorkImpl(parse);
            try
            {
               events.add(figureRef);
            }
            catch(Exception e)
            {
               System.out.println();
            }
         }
      }
      catch (Exception e)
      {
         System.out.println("Error" + e);
      }
      eIterable = events;
      return eIterable;
   }
}