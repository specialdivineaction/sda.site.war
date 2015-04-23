package edu.tamu.tcat.trc.entries.bio.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.catalogentries.CatalogRepoException;
import edu.tamu.tcat.catalogentries.IdFactory;
import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.db.exec.sql.SqlExecutor.ExecutorTask;
import edu.tamu.tcat.sda.catalog.psql.ExecutionFailedException;
import edu.tamu.tcat.sda.catalog.psql.ObservableTaskWrapper;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.bio.EditPeopleCommand;
import edu.tamu.tcat.trc.entries.bio.PeopleChangeEvent;
import edu.tamu.tcat.trc.entries.bio.PeopleChangeEvent.ChangeType;
import edu.tamu.tcat.trc.entries.bio.PeopleRepository;
import edu.tamu.tcat.trc.entries.bio.Person;
import edu.tamu.tcat.trc.entries.bio.PersonName;
import edu.tamu.tcat.trc.entries.bio.PersonNotAvailableException;
import edu.tamu.tcat.trc.entries.bio.dv.PersonDV;

public class PsqlPeopleRepo implements PeopleRepository
{
   private static final Logger logger = Logger.getLogger(PsqlPeopleRepo.class.getName());

   private static final String ID_CONTEXT = "people";
   private SqlExecutor exec;
   private IdFactory idFactory;
   private ObjectMapper mapper;

   private ExecutorService notifications;
   private final CopyOnWriteArrayList<Consumer<PeopleChangeEvent>> listeners = new CopyOnWriteArrayList<>();

   public PsqlPeopleRepo()
   {
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setIdFactory(IdFactory factory)
   {
      this.idFactory = factory;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(idFactory);

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      notifications = Executors.newCachedThreadPool();
   }

   public void dispose()
   {
      this.mapper = null;
      this.exec = null;
      this.idFactory = null;
      shutdownNotificationsExec();
   }

   private void shutdownNotificationsExec()
   {
      try
      {
         notifications.shutdown();
         notifications.awaitTermination(10, TimeUnit.SECONDS);    // HACK: make this configurable
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to shut down event notifications executor in a timely fashion.", ex);
         try {
            List<Runnable> pendingTasks = notifications.shutdownNow();
            logger.info("Forcibly shutdown notifications executor. [" + pendingTasks.size() + "] pending tasks were aborted.");
         } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred attempting to forcibly shutdown executor service", e);
         }
      }
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
         if (p.getCanonicalName().getFamilyName().toLowerCase().startsWith(prefix)) {
            results.add(p);
            continue;
         }

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
   public Person get(String personId) throws NoSuchCatalogRecordException
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
   public EditPeopleCommand create()
   {
      PersonDV person = new PersonDV();
      person.id = idFactory.getNextId(ID_CONTEXT);

      EditPeopleCommandImpl command = new EditPeopleCommandImpl(person, idFactory);
      command.setCommitHook((p) -> {
         CreatePersonTask task = new CreatePersonTask(person);
         PeopleChangeNotifier peopleChangeNotifier = new PeopleChangeNotifier(person.id, ChangeType.CREATED);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(task, peopleChangeNotifier);

         return exec.submit(wrappedTask);
      });


      return command;
   }

   @Override
   public EditPeopleCommand update(PersonDV personDV) throws NoSuchCatalogRecordException
   {
      EditPeopleCommandImpl command = new EditPeopleCommandImpl(personDV, idFactory);
      command.setCommitHook((p) -> {
         UpdatePersonTask task = new UpdatePersonTask(p);
         PeopleChangeNotifier peopleChangeNotifier = new PeopleChangeNotifier(personDV.id, ChangeType.MODIFIED);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(task, peopleChangeNotifier);

         return exec.submit(wrappedTask);
      });


      return command;
   }

   @Override
   public EditPeopleCommand delete(final String personId) throws NoSuchCatalogRecordException
   {
      PersonDV dto = PersonDV.create(get(personId));
      EditPeopleCommandImpl command = new EditPeopleCommandImpl(dto, idFactory);
      command.setCommitHook((p) -> {
         DeletePersonTask task = new DeletePersonTask(personId);
         PeopleChangeNotifier peopleChangeNotifier = new PeopleChangeNotifier(personId, ChangeType.DELETED);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(task, peopleChangeNotifier);

         return exec.submit(wrappedTask);
      });


      return command;
   }

   private PGobject toPGobject(final PersonDV histFigure) throws SQLException, IOException
   {
      PGobject jsonObject = new PGobject();
      jsonObject.setType("json");
      jsonObject.setValue(mapper.writeValueAsString(histFigure));
      return jsonObject;
   }

   private final class DeletePersonTask implements SqlExecutor.ExecutorTask<String>
   {
      private final static String delete_sql =  "UPDATE people " +
                                                    "SET active = false, " +
                                                    "  modified = now()  " +
                                                    "WHERE id = ?";
      private final String personId;

      private DeletePersonTask(String personId)
      {
         this.personId = personId;
      }

      @Override
      public String execute(Connection conn) throws Exception
      {
         try (PreparedStatement ps = conn.prepareStatement(delete_sql))
         {
            ps.setString(1, personId);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to de-activate historical figure. Unexpected number of rows updates [" + ct + "]");
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Faield to de-activate personId:" + personId, e);
         }

         return personId;

      }

   }

   private final static String GET_PERSON_SQL = "SELECT historical_figure FROM people WHERE id = ?";
   private final static String GET_ALL_SQL = "SELECT historical_figure FROM people WHERE active = true";
   private static final String UPDATE_SQL = "UPDATE people SET historical_figure = ?, modified = now() WHERE id = ?";

   private final class GetPersonTask implements ExecutorTask<Person>
   {

      private final String personId;

      private GetPersonTask(String personId)
      {
         this.personId = personId;
      }

      @Override
      public Person execute(Connection conn) throws NoSuchCatalogRecordException, InterruptedException
      {
         if (Thread.interrupted())
            throw new InterruptedException();

         try (PreparedStatement ps = conn.prepareStatement(GET_PERSON_SQL))
         {
            ps.setString(1, personId);
            try (ResultSet rs = ps.executeQuery())
            {
               if (!rs.next())
                  throw new NoSuchCatalogRecordException("Could not find record for person [" + personId + "]");

               PGobject pgo = (PGobject)rs.getObject("historical_figure");
               return parseJson(pgo.toString(), mapper);
            }
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Faield to retrieve person.", e);
         }
      }
   }

   private static Person parseJson(String json, ObjectMapper mapper)
   {
      try
      {
         PersonDV dv = mapper.readValue(json, PersonDV.class);
         return PersonDV.instantiate(dv);
      }
      catch (IOException je)
      {
         // NOTE: possible data leak. If this exception is propagated to someone who isn't authorized to see this record...
         throw new IllegalStateException("Cannot parse person from JSON:\n" + json, je);
      }
   }

   private final class GetAllPeopleTask implements ExecutorTask<List<Person>>
   {

      private GetAllPeopleTask()
      {
      }

      @Override
      public List<Person> execute(Connection conn) throws Exception
      {
         List<Person> people = new ArrayList<Person>();
         try (PreparedStatement ps = conn.prepareStatement(GET_ALL_SQL);
              ResultSet rs = ps.executeQuery())
         {

            while (rs.next())
            {
               PGobject pgo = (PGobject)rs.getObject("historical_figure");
               people.add(parseJson(pgo.toString(), mapper));
            }
         }

         return people;
      }
   }

   private final class UpdatePersonTask implements SqlExecutor.ExecutorTask<String>
   {


      private final PersonDV histFigure;

      private UpdatePersonTask(PersonDV histFigure)
      {
         this.histFigure = histFigure;
      }

      @Override
      public String execute(Connection conn) throws SQLException
      {
         try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL))
         {
            PGobject jsonObject = toPGobject(histFigure);
            ps.setObject(1, jsonObject);
            ps.setString(2, histFigure.id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to create historical figure. Unexpected number of rows updates [" + ct + "]");
         }
         catch (IOException e)
         {
            throw new IllegalArgumentException("Failed to serialize the supplied historical figure [" + histFigure + "]", e);
         }

         return histFigure.id;
      }
   }

   private final class CreatePersonTask implements SqlExecutor.ExecutorTask<String>
   {
      private static final String INSERT_SQL = "INSERT INTO people (id, historical_figure) VALUES(?, ?)";

      private final PersonDV histFigure;

      private CreatePersonTask(PersonDV histFigure)
      {
         this.histFigure = histFigure;
      }

      @Override
      public String execute(Connection conn) throws InterruptedException, ExecutionFailedException
      {
         try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL))
         {
            PGobject jsonObject = toPGobject(histFigure);

            ps.setString(1, histFigure.id);
            ps.setObject(2, jsonObject);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new ExecutionFailedException("Failed to create historical figure. Unexpected number of rows updates [" + ct + "]");
         }
         catch (IOException e)
         {
            // NOTE this is an internal configuration error. The JsonMapper should be configured to
            //      serialize HistoricalFigureDV instances correctly.
            throw new ExecutionFailedException("Failed to serialize the supplied historical figure [" + histFigure + "]", e);
         }
         catch (SQLException sqle)
         {
            throw new ExecutionFailedException("Failed to save historical figure [" + histFigure + "]", sqle);
         }

         return histFigure.id;
      }
   }

   @Override
   public AutoCloseable addUpdateListener(Consumer<PeopleChangeEvent> ears)
   {
      listeners.add(ears);
      return () -> listeners.remove(ears);
   }

   private class PeopleChangeEventImpl implements PeopleChangeEvent
   {
      private final ChangeType type;
      private final String id;

      public PeopleChangeEventImpl(ChangeType type, String id)
      {
         this.type = type;
         this.id = id;
      }

      @Override
      public ChangeType getChangeType()
      {
         return type;
      }

      @Override
      public String getPersonId()
      {
         return id;
      }

      @Override
      public Person getPerson() throws PersonNotAvailableException
      {
         try
         {
            return get(id);
         }
         catch (NoSuchCatalogRecordException e)
         {
            throw new PersonNotAvailableException("Internal error attempting to retrieve person [" + id + "]");
         }
      }

   }

   private final class PeopleChangeNotifier extends DataUpdateObserverAdapter<String>
   {
      private final String id;
      private final ChangeType type;

      public PeopleChangeNotifier(String id, ChangeType type)
      {
         this.type = type;
         this.id = id;
      }

      @Override
      public void onFinish(String result)
      {
         notifyPersonUpdate(type, id);
      }
   }

   private void notifyPersonUpdate(ChangeType type, String id)
   {
      PeopleChangeEventImpl evt = new PeopleChangeEventImpl(type, id);
      listeners.forEach(ears -> {
         notifications.submit(() -> {
            try{
               ears.accept(evt);
            }
            catch(Exception ex)
            {
               logger.log(Level.WARNING, "Call to update people listener failed.", ex);
            }
         });
      });
   }
}