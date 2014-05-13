package edu.tamu.tcat.sda.catalog.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.events.HistoricalEvent;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigureRepository;
import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.ds.DataUpdateObserver;

public class PsqlHistoricalFigureRepo implements HistoricalFigureRepository
{
   
   private final DbExecutor exec;
   private final JsonMapper jsonMapper;

   public PsqlHistoricalFigureRepo(DbExecutor exec, JsonMapper jsonMapper)
   {
      this.exec = exec;
      this.jsonMapper = jsonMapper;
   }

   @Override
   public Iterable<HistoricalFigure> listHistoricalFigures()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void create(final HistoricalFigureDV histFigure, DataUpdateObserver<HistoricalFigure> observer)
   {
      final String insertSql = "INSERT INTO people (historical_figure) VALUES(null)";
      final String updateSql = "UPDATE people "
                               + " SET historical_figure = ?"
                               + " WHERE id = ?";
      DbExecTask<HistoricalFigure> task1 = new DbExecTask<HistoricalFigure>()
      {
         @Override
         public HistoricalFigure execute(Connection conn) throws SQLException
         {
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS))
            {

               int ct = ps.executeUpdate();
               if (ct != 1)
                  throw new IllegalStateException("Failed to create historical figure. Unexpected number of rows updates [" + ct + "]");
               else
               {
                  ResultSet rs = ps.getGeneratedKeys();
                  if ( rs.next() )
                  {
                      int key = rs.getInt(1);
                      histFigure.id = new Integer(key).toString();
                  }
               }
            }
            
            try (PreparedStatement ps = conn.prepareStatement(updateSql))
            {
               PGobject jsonObject = new PGobject();
               jsonObject.setType("json");
               jsonObject.setValue(jsonMapper.asString(histFigure));

               ps.setObject(1, jsonObject);
               ps.setInt(2, Integer.parseInt(histFigure.id));

               int ct = ps.executeUpdate();
               if (ct != 1)
                  throw new IllegalStateException("Failed to create historical figure. Unexpected number of rows updates [" + ct + "]");

            }
            catch (JsonException e)
            {
               throw new IllegalArgumentException("Failed to serialize the supplied historical figure [" + histFigure + "]", e);
            }
            return new HistoricalFigureRef(Long.parseLong(histFigure.id));
         }
      };
      
      exec.submit(new ObservableTaskWrapper<>(task1, observer));
      
   }

   @Override
   public void update(final HistoricalFigureDV histFigure, DataUpdateObserver<HistoricalFigure> observer)
   {
      final String updateSql = "UPDATE people "
            + " SET historical_figure = ?"
            + " WHERE id = ?";
      DbExecTask<HistoricalFigure> task1 = new DbExecTask<HistoricalFigure>()
      {
         @Override
         public HistoricalFigure execute(Connection conn) throws SQLException
         {
            try (PreparedStatement ps = conn.prepareStatement(updateSql))
            {
               PGobject jsonObject = new PGobject();
               jsonObject.setType("json");
               jsonObject.setValue(jsonMapper.asString(histFigure));
               
               ps.setObject(1, jsonObject);
               ps.setInt(2, Integer.parseInt(histFigure.id));
               
               int ct = ps.executeUpdate();
               if (ct != 1)
               throw new IllegalStateException("Failed to create historical figure. Unexpected number of rows updates [" + ct + "]");
               
            }
            catch (JsonException e)
            {
               throw new IllegalArgumentException("Failed to serialize the supplied historical figure [" + histFigure + "]", e);
            }
            return new HistoricalFigureRef(Long.parseLong(histFigure.id));
         }
      };
      
      exec.submit(new ObservableTaskWrapper<>(task1, observer));      // TODO Auto-generated method stub
      
   }

   private static class HistoricalFigureRef implements HistoricalFigure
   {
      public HistoricalFigureRef(long id)
      {
         
      }

      @Override
      public String getId()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public PersonName getCanonicalName()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Set<PersonName> getAlternativeNames()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public HistoricalEvent getBirth()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public HistoricalEvent getDeath()
      {
         // TODO Auto-generated method stub
         return null;
      }
      
   }
}
