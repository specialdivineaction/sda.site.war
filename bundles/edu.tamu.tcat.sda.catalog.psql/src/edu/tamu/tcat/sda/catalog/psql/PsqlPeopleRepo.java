package edu.tamu.tcat.sda.catalog.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.db.exec.sql.SqlExecutor.ExecutorTask;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.CatalogRepoException;
import edu.tamu.tcat.sda.catalog.IdFactory;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.people.PeopleRepository;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.catalog.psql.impl.PersonImpl;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class PsqlPeopleRepo implements PeopleRepository
{
   private static final String ID_CONTEXT = "people";
   private SqlExecutor exec;
   private IdFactory idFactory;
   private JsonMapper jsonMapper;

   public PsqlPeopleRepo()
   {
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setJsonMapper(JsonMapper mapper)
   {
      this.jsonMapper = mapper;
   }

   public void setIdFactory(IdFactory factory)
   {
      this.idFactory = factory;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(jsonMapper);
   }

   public void dispose()
   {
      this.exec = null;
      this.jsonMapper = null;
   }


   @Override
   public Iterable<Person> findPeople() throws CatalogRepoException
   {
      ExecutorTask<List<Person>> task = new GetAllPeopleTask();
      Future<List<Person>> future = exec.submit(task);
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
      try {
         return getPerson(Long.parseLong(personId));
      }
      catch (NumberFormatException nfe)
      {
         throw new NoSuchCatalogRecordException("Could not find record for person [" + personId + "]");
      }
   }

   @Override
   public Person getPerson(long personId) throws NoSuchCatalogRecordException
   {
      ExecutorTask<Person> query = new GetPersonTask(personId);

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

   // TODO implement PersonBuilder pattern

   @Override
   public void create(final PersonDV histFigure, final DataUpdateObserver<Person> observer)
   {
      histFigure.id = idFactory.getNextId(ID_CONTEXT);

      ExecutorTask<Person> createPersonTask = new CreatePersonTask(histFigure);
      exec.submit(new ObservableTaskWrapper<>(createPersonTask, observer));
   }

   @Override
   public void update(final PersonDV histFigure, DataUpdateObserver<Person> observer)
   {
      ExecutorTask<Person> updateTask = new UpdatePersonTask(histFigure);
      exec.submit(new ObservableTaskWrapper<>(updateTask, observer));
   }

   @Override
   public void delete(final String psrsonId, final DataUpdateObserver<Void> observer)
   {
      // TODO IMPLEMENT
      throw new UnsupportedOperationException();
      // TODO: Add another column for active.
//      final String updateSql = "";
//      SqlExecutor.ExecutorTask<Void> deleteTask = new SqlExecutor.ExecutorTask<Void>()
//      {
//         @Override
//         public Void execute(Connection conn) throws SQLException
//         {
//            observer.error("Not Implmented", new UnsupportedOperationException());
//            return null;
//         }
//      };
//
//      exec.submit(new ObservableTaskWrapper<>(deleteTask, observer));
   }

   private PGobject toPGobject(final PersonDV histFigure) throws SQLException, JsonException
   {
      PGobject jsonObject = new PGobject();
      jsonObject.setType("json");
      jsonObject.setValue(jsonMapper.asString(histFigure));
      return jsonObject;
   }

   private final class GetPersonTask implements ExecutorTask<Person>
   {
      private final static String SQL = "SELECT historical_figure FROM people WHERE id=?";

      private final long personId;

      private GetPersonTask(long personId)
      {
         this.personId = personId;
      }

      @Override
      public Person execute(Connection conn) throws NoSuchCatalogRecordException, InterruptedException
      {
         if (Thread.interrupted())
            throw new InterruptedException();

         try (PreparedStatement ps = conn.prepareStatement(SQL))
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
   }

   private final class GetAllPeopleTask implements ExecutorTask<List<Person>>
   {
      private final static String QUERY_SQL = "SELECT historical_figure FROM people";

      private GetAllPeopleTask()
      {
      }

      @Override
      public List<Person> execute(Connection conn) throws Exception
      {
         List<Person> people = new ArrayList<Person>();
         try (PreparedStatement ps = conn.prepareStatement(QUERY_SQL);
              ResultSet rs = ps.executeQuery())
         {

            while (rs.next())
            {
               PGobject pgo = (PGobject)rs.getObject("historical_figure");

               PersonDV parse = jsonMapper.parse(pgo.toString(), PersonDV.class);
               PersonImpl person = new PersonImpl(parse);
               people.add(person);
            }
         }

         return people;
      }
   }

   private final class UpdatePersonTask implements ExecutorTask<Person>
   {

      private static final String UPDATE_SQL = "UPDATE people SET historical_figure = ? WHERE id = ?";

      private final PersonDV histFigure;

      private UpdatePersonTask(PersonDV histFigure)
      {
         this.histFigure = histFigure;
      }

      @Override
      public Person execute(Connection conn) throws SQLException
      {
         try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL))
         {
            PGobject jsonObject = toPGobject(histFigure);
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
   }

   private final class CreatePersonTask implements SqlExecutor.ExecutorTask<Person>
   {
      private static final String INSERT_SQL = "INSERT INTO people (id, historical_figure) VALUES(?, ?)";

      private final PersonDV histFigure;

      private CreatePersonTask(PersonDV histFigure)
      {
         this.histFigure = histFigure;
      }

      @Override
      public Person execute(Connection conn) throws InterruptedException, ExecutionFailedException
      {
         try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL))
         {
            PGobject jsonObject = toPGobject(histFigure);

            ps.setInt(1, Integer.parseInt(histFigure.id));     // FIXME change to string
            ps.setObject(2, jsonObject);

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
   }
}