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
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.CatalogRepoException;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.people.PeopleRepository;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.catalog.psql.impl.PersonImpl;
import edu.tamu.tcat.sda.catalog.solr.AuthorController;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class PsqlPeopleRepo implements PeopleRepository
{
   private static final Logger DbTaskLogger = Logger.getLogger("edu.tamu.tcat.sda.catalog.people.db.errors");

   private SqlExecutor exec;
   private JsonMapper jsonMapper;
   private long personId;
   private AuthorController authController;

   public PsqlPeopleRepo()
   {
      authController = new AuthorController();
   }

   public void setDatabaseExecutor(SqlExecutor exec)
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

      SqlExecutor.ExecutorTask<List<Person>> query = new SqlExecutor.ExecutorTask<List<Person>>()
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
      this.personId = Long.parseLong(personId);
      try {
         return getPerson(this.personId);
      }
      catch (NumberFormatException nfe)
      {
         throw new NoSuchCatalogRecordException("Could not find record for person [" + personId + "]");
      }
   }

   @Override
   public Person getPerson(final long personId) throws NoSuchCatalogRecordException
   {
      final String querySql = "SELECT historical_figure FROM people WHERE id=?";
      this.personId = personId;
      SqlExecutor.ExecutorTask<Person> query = new SqlExecutor.ExecutorTask<Person>()
      {
         @Override
         public Person execute(Connection conn) throws NoSuchCatalogRecordException, InterruptedException
         {
            if (Thread.interrupted())
               throw new InterruptedException();

            try (PreparedStatement ps = conn.prepareStatement(querySql))
            {
               PersonImpl result;
               ps.setLong(1, personId);
               try (ResultSet rs = ps.executeQuery())
               {
                  if (!rs.next())
                     throw new NoSuchCatalogRecordException("Could not find record for person [" + personId + "]");

                  PGobject pgo = (PGobject)rs.getObject("historical_figure");
                  String json = pgo.toString();
                  try
                  {
                     PersonDV dv = jsonMapper.parse(json, PersonDV.class);
                     result = new PersonImpl(dv);
                  }
                  catch (JsonException je)
                  {
                     // NOTE: possible data leak. If this exception is propagated to someone who isn't authorized to see this record...
                     throw new IllegalStateException("Cannot parse person from JSON:\n" + json, je);
                  }
               }

               return result;
            }
            catch (SQLException e)
            {
               throw new IllegalStateException("Faield to retrieve person.", e);
            }
         }
      };

      try
      {
         return exec.submit(query).get();
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof NoSuchCatalogRecordException)
            throw (NoSuchCatalogRecordException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve biographical record [" + personId + "]", e);
      }
      catch (InterruptedException e) {
         throw new IllegalStateException("Failed to retrieve biographical record [" + personId + "]", e);
      }
   }


   @Override
   public void create(final PersonDV histFigure, final DataUpdateObserver<Person> observer)
   {
      final String insertSql = "INSERT INTO people (historical_figure) VALUES(null)";
      final String updateSql = "UPDATE people "
                               + " SET historical_figure = ?"
                               + " WHERE id = ?";

      SqlExecutor.ExecutorTask<Person> createPersonTask = new SqlExecutor.ExecutorTask<Person>()
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

               authController.addDocument(histFigure);
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
      SqlExecutor.ExecutorTask<Person> task1 = new SqlExecutor.ExecutorTask<Person>()
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
            authController.addDocument(histFigure);
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
      SqlExecutor.ExecutorTask<Void> deleteTask = new SqlExecutor.ExecutorTask<Void>()
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