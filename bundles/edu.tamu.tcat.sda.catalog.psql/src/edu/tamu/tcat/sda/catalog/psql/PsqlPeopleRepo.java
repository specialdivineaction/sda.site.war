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
import edu.tamu.tcat.sda.catalog.CatalogRepoException;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.people.PeopleRepository;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.catalog.psql.impl.PersonImpl;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class PsqlPeopleRepo implements PeopleRepository
{
   // private static final Logger DbTaskLogger = Logger.getLogger("edu.tamu.tcat.sda.catalog.people.db.errors");

   private DbExecutor exec;
   private JsonMapper jsonMapper;

   public PsqlPeopleRepo()
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
   public Iterable<Person> findPeople() throws CatalogRepoException
   {

      final String querySql = "SELECT historical_figure FROM people";

      DbExecTask<List<Person>> query = new DbExecTask<List<Person>>()
      {

         @Override
         public List<Person> execute(Connection conn) throws Exception
         {
            List<Person> people = new ArrayList<Person>();
            try (PreparedStatement ps = conn.prepareStatement(querySql);
                 ResultSet rs = ps.executeQuery())
            {

               while (rs.next())
               {
                  PGobject pgo = (PGobject)rs.getObject("historical_figure");

                  PersonDV parse = jsonMapper.parse(pgo.toString(), PersonDV.class);
                  PersonImpl figureRef = new PersonImpl(parse);
                  people.add(figureRef);
               }
            }

            return people;
         }
      };

      Future<List<Person>> future = exec.submit(query);
      try
      {
         return future.get();
      }
      catch (Exception e)
      {
         throw new CatalogRepoException("Failed to retrieve people", e);
      }
   }

   @Override
   public Iterable<Person> findByName(String prefix) throws CatalogRepoException
   {
      List<Person> results = new ArrayList<>();
      prefix = prefix.toLowerCase();

      Iterable<Person> people = findPeople();
      for (Person p : people)
      {
         for (PersonName name : p.getAlternativeNames())
         {
            String fname = name.getFamilyName();
            if (fname != null && fname.toLowerCase().startsWith(prefix))
            {
               results.add(p);
               break;
            }
         }
      }

      return results;
   }

   @Override
   public Person getPerson(String personId) throws NoSuchCatalogRecordException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Person getPerson(long personId) throws NoSuchCatalogRecordException
   {
      // TODO Jesse - update to use string-based identifiers. Keep DB autoincremnt long around but add
      //      additional field. For now, we'll use the supplied DB id as the string id, but this may
      //      change in the near future.
      //      THEN, delegate this method to getPerson(String).
//      return getPerson(Long.toString(personId));
      final String querySql = "SELECT historical_figure FROM people WHERE id=?";
      final long id = personId;
      DbExecTask<Person> query = new DbExecTask<Person>()
      {
         PersonImpl figureRef;
         @Override
         public Person execute(Connection conn) throws Exception
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

                     PersonDV parse = jsonMapper.parse(pgo.toString(), PersonDV.class);
                     figureRef = new PersonImpl(parse);
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

      Person submit = null;
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
   public void create(final PersonDV histFigure, final DataUpdateObserver<Person> observer)
   {
      final String insertSql = "INSERT INTO people (historical_figure) VALUES(null)";
      final String updateSql = "UPDATE people "
                               + " SET historical_figure = ?"
                               + " WHERE id = ?";

      DbExecTask<Person> createPersonTask = new DbExecTask<Person>()
      {
         private final String createPersonId(Connection conn) throws InterruptedException, ExecutionFailedException
         {
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS))
            {
               if (observer != null && observer.isCanceled())
                  throw new InterruptedException();

               ps.executeUpdate();
               try (ResultSet rs = ps.getGeneratedKeys())
               {
                  if (!rs.next())
                     throw new ExecutionFailedException("Failed to generate id for historical figure [" + histFigure + "]");

                  return Integer.toString(rs.getInt(1));
               }
            }
            catch (SQLException sqle)
            {
               throw new ExecutionFailedException("Failed to generate id for historical figure [" + histFigure + "]", sqle);
            }
         }

         private Person savePersonDetails(Connection conn) throws InterruptedException, ExecutionFailedException
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

               return new PersonImpl(histFigure);
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
         public Person execute(Connection conn) throws InterruptedException, ExecutionFailedException
         {
            histFigure.id = createPersonId(conn);
            Person result = savePersonDetails(conn);

            return result;
         }
      };

      exec.submit(new ObservableTaskWrapper<>(createPersonTask, observer));
   }

   @Override
   public void update(final PersonDV histFigure, DataUpdateObserver<Person> observer)
   {
      final String updateSql = "UPDATE people "
            + " SET historical_figure = ?"
            + " WHERE id = ?";
      DbExecTask<Person> task1 = new DbExecTask<Person>()
      {
         @Override
         public Person execute(Connection conn) throws SQLException
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
            return new PersonImpl(histFigure);
         }
      };

      exec.submit(new ObservableTaskWrapper<>(task1, observer));      // TODO Auto-generated method stub
   }

   @Override
   public void delete(final String psrsonId, final DataUpdateObserver<Void> observer)
   {
      // TODO: Add another column for active.
      final String updateSql = "";
      DbExecTask<Void> deleteTask = new DbExecTask<Void>()
      {
         @Override
         public Void execute(Connection conn) throws SQLException
         {
            observer.error("Not Implmented", new UnsupportedOperationException());
            return null;
         }
      };

      exec.submit(new ObservableTaskWrapper<>(deleteTask, observer));      // TODO Auto-generated method stub
   }
}