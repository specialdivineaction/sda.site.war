package edu.tamu.tcat.sda.catalog.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.db.ExecutionFailedException;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigureRepository;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.catalog.psql.impl.HistoricalFigureImpl;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class PsqlHistoricalFigureRepo implements HistoricalFigureRepository
{

   private DbExecutor exec;
   private JsonMapper jsonMapper;

   public PsqlHistoricalFigureRepo()
   {
   }

   public void setDatabaseExecutor(DbExecutor exec)
   {
      this.exec = exec;
   }

   public void setJsonMapper(JsonMapper mapper)
   {
      this.jsonMapper = mapper;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(jsonMapper);
   }

   public void dispose()
   {
      // TODO wait on or cancel any pending tasks?

      this.exec = null;
      this.jsonMapper = null;
   }


   @Override
   public Iterable<HistoricalFigure> listHistoricalFigures()
   {

      // FIXME this is async, meaning test will exit prior to conclusion.
      // TODO Auto-generated method stub
      final String querySql = "SELECT historical_figure FROM people";

      DbExecTask<Iterable<HistoricalFigure>> query = new DbExecTask<Iterable<HistoricalFigure>>()
      {

         @Override
         public Iterable<HistoricalFigure> execute(Connection conn) throws Exception
         {
            List<HistoricalFigure> events = new ArrayList<HistoricalFigure>();
            Iterable<HistoricalFigure> eIterable = new ArrayList<HistoricalFigure>();
            try (PreparedStatement ps = conn.prepareStatement(querySql);
                 ResultSet rs = ps.executeQuery())
            {
               PGobject pgo = new PGobject();

               while(rs.next())
               {
                  Object object = rs.getObject("historical_figure");
                  if (object instanceof PGobject)
                     pgo = (PGobject)object;
                  else
                     System.out.println("Error!");

                  HistoricalFigureDV parse = jsonMapper.parse(pgo.toString(), HistoricalFigureDV.class);
                  HistoricalFigureImpl figureRef = new HistoricalFigureImpl(parse);
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
//            latch.countDown();
            eIterable = events;
            return eIterable;
         }

      };

      Future<Iterable<HistoricalFigure>> submit = exec.submit(query);
      Iterable<HistoricalFigure> iterable = null;
      try
      {
         iterable = submit.get();
      }
      catch (InterruptedException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (ExecutionException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return  iterable;
   }

   @Override
   public HistoricalFigure getPerson(long personId)
   {
      final String querySql = "SELECT historical_figure FROM people WHERE id=?";
      final long id = personId;
      DbExecTask<HistoricalFigure> query = new DbExecTask<HistoricalFigure>()
      {
         HistoricalFigureImpl figureRef;
         @Override
         public HistoricalFigure execute(Connection conn) throws Exception
         {
            try (PreparedStatement ps = conn.prepareStatement(querySql))
            {
               ps.setLong(1, id);
               try (ResultSet rs = ps.executeQuery())
               {
                  PGobject pgo = new PGobject();

                  while(rs.next())
                  {
                     Object object = rs.getObject("historical_figure");
                     if (object instanceof PGobject)
                        pgo = (PGobject)object;
                     else
                        System.out.println("Error!");

                     HistoricalFigureDV parse = jsonMapper.parse(pgo.toString(), HistoricalFigureDV.class);
                     figureRef = new HistoricalFigureImpl(parse);
                  }
               }
            }
            catch (Exception e)
            {
               System.out.println("Error" + e);
            }
            return figureRef;
         }

      };

      HistoricalFigure submit = null;
      try
      {
         return exec.submit(query).get();
      }
      catch (InterruptedException | ExecutionException e)
      {
         System.out.println("Error");
      }

      return submit;
   }


   @Override
   public void create(final HistoricalFigureDV histFigure, final DataUpdateObserver<HistoricalFigure> observer)
   {
      final String insertSql = "INSERT INTO people (historical_figure) VALUES(null)";
      final String updateSql = "UPDATE people "
                               + " SET historical_figure = ?"
                               + " WHERE id = ?";
      
      DbExecTask<HistoricalFigure> createPersonTask = new DbExecTask<HistoricalFigure>()
      {
         private final String createPersonId(Connection conn) throws InterruptedException, ExecutionFailedException
         {
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS))
            {
               if (observer != null && observer.isCanceled())
                  throw new InterruptedException();
               
               ps.executeUpdate();
               ResultSet rs = ps.getGeneratedKeys();
               if (!rs.next())
                  throw new ExecutionFailedException("Failed to generate id for historical figure [" + histFigure + "]");
               
               return Integer.toString(rs.getInt(1));

            }
            catch (SQLException sqle)
            {
               throw new ExecutionFailedException("Failed to generate id for historical figure [" + histFigure + "]", sqle);
            }
         }
         
         private HistoricalFigure savePersonDetails(Connection conn) throws InterruptedException, ExecutionFailedException
         {
            try (PreparedStatement ps = conn.prepareStatement(updateSql))
            {
               PGobject jsonObject = new PGobject();
               jsonObject.setType("json");
               jsonObject.setValue(jsonMapper.asString(histFigure));
         
               ps.setObject(1, jsonObject);
               ps.setInt(2, Integer.parseInt(histFigure.id));
         
               if (observer != null && observer.isCanceled())
                  throw new InterruptedException();
               
               int ct = ps.executeUpdate();
               if (ct != 1)
                  throw new ExecutionFailedException("Failed to create historical figure. Unexpected number of rows updates [" + ct + "]");
         
               return new HistoricalFigureImpl(histFigure);
            }
            catch (JsonException e)
            {
               // NOTE this is an internal configuration error. The JsonMapper should be configured to 
               //      serialize HistoricalFigureDV instances correctly.
               throw new ExecutionFailedException("Failed to serialize the supplied historical figure [" + histFigure + "]", e);
            }
            catch (SQLException sqle)
            {
               throw new ExecutionFailedException("Failed to save historical figure [" + histFigure + "]", sqle);
            }
         }

         @Override
         public HistoricalFigure execute(Connection conn) throws InterruptedException, ExecutionFailedException
         {
            histFigure.id = createPersonId(conn);
            HistoricalFigure result = savePersonDetails(conn);
            
            return result;
         }
      };
      
      exec.submit(new ObservableTaskWrapper<>(createPersonTask, observer));
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
            return new HistoricalFigureImpl(histFigure);
         }
      };

      exec.submit(new ObservableTaskWrapper<>(task1, observer));      // TODO Auto-generated method stub
   }
}
