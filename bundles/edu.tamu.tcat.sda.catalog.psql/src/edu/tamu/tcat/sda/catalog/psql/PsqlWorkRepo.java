package edu.tamu.tcat.sda.catalog.psql;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.CommandExecutionListener;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.people.PeopleRepository;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.psql.tasks.PsqlCreateWorkTask;
import edu.tamu.tcat.sda.catalog.psql.tasks.PsqlListWorksTask;
import edu.tamu.tcat.sda.catalog.psql.tasks.PsqlUpdateWorksTask;
import edu.tamu.tcat.sda.catalog.psql.tasks.PsqlWorkDbTasksProvider;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.EditWorkCommand;
import edu.tamu.tcat.sda.catalog.works.Edition;
import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;
import edu.tamu.tcat.sda.catalog.works.Volume;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class PsqlWorkRepo implements WorkRepository
{
   private SqlExecutor exec;
   private JsonMapper jsonMapper;
   private PeopleRepository peopleRepo;
   private PsqlWorkDbTasksProvider taskProvider;

   private static Map<String, WeakReference<IdFactory>> idProviders = new HashMap<>();

   private static IdFactory getIdFactory(Work work)
   {
      if (null == work) {
         return new IdFactory();
      }

      String workId = work.getId();

      if (null == workId) {
         return new IdFactory();
      }

      synchronized (idProviders) {
         if (idProviders.containsKey(workId) && null != idProviders.get(workId).get()) {
            return idProviders.get(workId).get();
         }

         int maxId = work.getEditions().parallelStream()
               .mapToInt((e) -> Integer.parseInt(e.getId()))
               .max().orElse(1);

         IdFactory provider = new IdFactory(maxId);
         idProviders.put(workId, new WeakReference<>(provider));
         return provider;
      }
   }

   public PsqlWorkRepo()
   {
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setPeopleRepo(PeopleRepository repo)
   {
      this.peopleRepo = repo;
   }

   public void setJsonMapper(JsonMapper mapper)
   {
      this.jsonMapper = mapper;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(jsonMapper);

      taskProvider = new PsqlWorkDbTasksProvider();
      taskProvider.setJsonMapper(jsonMapper);
   }

   public void dispose()
   {
      this.exec = null;
      this.jsonMapper = null;
   }

   @Override
   public Person getAuthor(AuthorReference ref)
   {
      String id = ref.getId();
      try {
         // FIXME repo should accept string identifiers.
         return peopleRepo.getPerson(Integer.parseInt(id));
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Could not retrieve person instance (" + id + ").", ex);
      }
   }

   @Override
   public Iterable<Work> listWorks()
   {
      PsqlListWorksTask task = taskProvider.makeListWorksTask();

      Future<Iterable<Work>> submit = exec.submit(task);
      Iterable<Work> iterable = null;
      try
      {
         // HACK: could block forever.
         iterable = submit.get();
      }
      catch (ExecutionException e)
      {
//         Throwable cause = e.getCause();
//         if (cause instanceof NoSuchCatalogRecordException)
//            throw (NoSuchCatalogRecordException)cause;
//         if (cause instanceof RuntimeException)
//            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve work records " , e);
      }
      catch (InterruptedException e) {
         throw new IllegalStateException("Failed to retrieve work records", e);
      }

      return  iterable;
   }

   @Override
   public void create(final WorkDV work, DataUpdateObserver<String> observer)
   {
      PsqlCreateWorkTask task = taskProvider.makeCreateWorkTask(work);
      exec.submit(new ObservableTaskWrapper<>(task, observer));
   }

   @Override
   public void update(WorkDV work, DataUpdateObserver<String> observer)
   {
      PsqlUpdateWorksTask task = taskProvider.makeUpdateWorksTask(work);
      exec.submit(new ObservableTaskWrapper<>(task, observer));
   }

   @Override
   public Iterable<Work> listWorks(String titleName)
   {
      List<Work> workResults = new ArrayList<>();
      Iterable<Work> listWorks = listWorks();
      titleName = titleName.toLowerCase();

      for (Work w : listWorks)
      {
         TitleDefinition titleDef = w.getTitle();

         for (Title t : titleDef.getAlternateTitles())
         {
            boolean titleFound = false;
            titleFound = hasTitleName(t, titleName);
            if (titleFound)
            {
               workResults.add(w);
               continue;
            }
         }
      }

      return workResults;
   }

   @Override
   public Work getWork(int workId) throws NoSuchCatalogRecordException
   {
      SqlExecutor.ExecutorTask<Work> task = taskProvider.makeGetWorkTask(workId);
      try
      {
         return exec.submit(task).get();
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof NoSuchCatalogRecordException)
            throw (NoSuchCatalogRecordException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve bibliographic entry [" + workId +"]" , e);
      }
      catch (InterruptedException e) {
         throw new IllegalStateException("Failed to retrieve bibliographic entry [" + workId +"]", e);
      }
   }

   @Override
   public Work getWork(String workId) throws NoSuchCatalogRecordException
   {
      return getWork(Integer.parseInt(workId));
   }

   @Override
   public Edition getEdition(String workId, String editionId) throws NoSuchCatalogRecordException
   {
      Work work = getWork(workId);
      return work.getEdition(editionId);
   }

   @Override
   public Volume getVolume(String workId, String editionId, String volumeId) throws NoSuchCatalogRecordException
   {
      Work work = getWork(workId);
      Edition edition = work.getEdition(editionId);
      return edition.getVolume(volumeId);
   }

   private boolean hasTitleName(Title title, String titleName)
   {
      String test = title.getFullTitle();
      if (test != null && test.toLowerCase().contains(titleName))
         return true;

      test = title.getTitle();
      if (test != null && test.toLowerCase().contains(titleName))
         return true;

      return false;
   }

   @Override
   public EditWorkCommand edit(String id) throws NoSuchCatalogRecordException
   {
      Work work = getWork(asInteger(id));
      EditWorkCommandImpl command = new EditWorkCommandImpl(new WorkDV(work), getIdFactory(work));
      command.setCommitHook((workDv) -> {
         PsqlUpdateWorksTask task = new PsqlUpdateWorksTask(workDv, jsonMapper);
         return exec.submit(task);
      });

      return command;
   }

   private int asInteger(String id)
   {
      try {
         return Integer.parseInt(id);
      }
      catch (NumberFormatException e) {
         throw new IllegalArgumentException("Malformed Work ID [" + id + "]", e);
      }
   }

   @Override
   public EditWorkCommand create()
   {
      EditWorkCommandImpl command = new EditWorkCommandImpl(new WorkDV(), getIdFactory(null));
      command.setCommitHook((workDv) -> {
         PsqlCreateWorkTask task = new PsqlCreateWorkTask(workDv, jsonMapper);
         return exec.submit(task);
      });
      return command;
   }

   @Override
   public AutoCloseable addBeforeUpdateListener(CommandExecutionListener ears)
   {
      throw new UnsupportedOperationException("not impl");
   }

   @Override
   public AutoCloseable addAfterUpdateListener(CommandExecutionListener ears)
   {
      throw new UnsupportedOperationException("not impl");
   }
}
