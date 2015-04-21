package edu.tamu.tcat.trc.entries.bib.postgres;

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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.catalogentries.IdFactory;
import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;
import edu.tamu.tcat.trc.entries.bib.AuthorReference;
import edu.tamu.tcat.trc.entries.bib.EditWorkCommand;
import edu.tamu.tcat.trc.entries.bib.Edition;
import edu.tamu.tcat.trc.entries.bib.Title;
import edu.tamu.tcat.trc.entries.bib.Volume;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.WorkNotAvailableException;
import edu.tamu.tcat.trc.entries.bib.WorkRepository;
import edu.tamu.tcat.trc.entries.bib.WorksChangeEvent;
import edu.tamu.tcat.trc.entries.bib.WorksChangeEvent.ChangeType;
import edu.tamu.tcat.trc.entries.bib.dto.EditionDV;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;
import edu.tamu.tcat.trc.entries.bio.PeopleRepository;
import edu.tamu.tcat.trc.entries.bio.Person;

public class PsqlWorkRepo implements WorkRepository
{

   private static final Logger logger = Logger.getLogger(PsqlWorkRepo.class.getName());
   public static final String WORK_CONTEXT = "works";

   private SqlExecutor exec;
   private ObjectMapper mapper;
   private PeopleRepository peopleRepo;
   private PsqlWorkDbTasksProvider taskProvider;

   private ExecutorService notifications;

   private final CopyOnWriteArrayList<Consumer<WorksChangeEvent>> listeners = new CopyOnWriteArrayList<>();

   private IdFactory idFactory;

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

   public void setIdFactory(IdFactory idFactory)
   {
      this.idFactory = idFactory;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      taskProvider = new PsqlWorkDbTasksProvider();
      taskProvider.setJsonMapper(mapper);

      notifications = Executors.newCachedThreadPool();
   }

   public void dispose()
   {
      this.exec = null;
      this.mapper = null;
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
   public Person getAuthor(AuthorReference ref)
   {
      String id = ref.getId();
      try {
         return peopleRepo.get(id);
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
//
//   @Override
//   public void create(final WorkDV work, DataUpdateObserver<String> observer)
//   {
//      PsqlCreateWorkTask task = taskProvider.makeCreateWorkTask(work);
//      exec.submit(new ObservableTaskWrapper<>(task, observer));
//   }
//
//   @Override
//   public void update(WorkDV work, DataUpdateObserver<String> observer)
//   {
//      PsqlUpdateWorksTask task = taskProvider.makeUpdateWorksTask(work);
//      exec.submit(new ObservableTaskWrapper<>(task, observer));
//   }

   @Override
   public Iterable<Work> listWorks(String titleName)
   {
      List<Work> workResults = new ArrayList<>();
      Iterable<Work> listWorks = listWorks();
      titleName = titleName.toLowerCase();

      for (Work w : listWorks)
      {
         if (hasTitle(w, titleName))
            workResults.add(w);
      }

      return workResults;
   }

   private boolean hasTitle(Work w, String name)
   {
      for (Title t : w.getTitle().getAlternateTitles())
      {
         if (hasTitleName(t, name)) {
            return true;
         }
      }

      return false;
   }
   @Override
   public Work getWork(String workId) throws NoSuchCatalogRecordException
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
      Work work = getWork(id);
      EditWorkCommandImpl command = new EditWorkCommandImpl(new WorkDV(work), idFactory);
      command.setCommitHook((workDv) -> {
         PsqlUpdateWorksTask task = new PsqlUpdateWorksTask(workDv, mapper);

         WorkChangeNotifier<String> workChangeNotifier = new WorkChangeNotifier<>(workDv.id, ChangeType.MODIFIED);
         ObservableTaskWrapper<String> wrapTask = new ObservableTaskWrapper<String>(task, workChangeNotifier);

         Future<String> submitWork = exec.submit(wrapTask);
         return submitWork;
      });

      return command;
   }

   @Override
   public EditWorkCommand create()
   {
      WorkDV work = new WorkDV();
      work.id = idFactory.getNextId(WORK_CONTEXT);
      EditWorkCommandImpl command = new EditWorkCommandImpl(work, idFactory);

      command.setCommitHook((w) -> {
         PsqlCreateWorkTask task = new PsqlCreateWorkTask(w, mapper);

         WorkChangeNotifier<String> workChangeNotifier = new WorkChangeNotifier<>(w.id, ChangeType.CREATED);
         ObservableTaskWrapper<String> wrapTask = new ObservableTaskWrapper<String>(task, workChangeNotifier);

         Future<String> submitWork = exec.submit(wrapTask);
         return submitWork;
      });

      return command;
   }

   @Override
   public EditWorkCommand delete(String id) throws NoSuchCatalogRecordException
   {
      Work work = getWork(id);
      EditWorkCommandImpl command = new EditWorkCommandImpl(new WorkDV(work), idFactory);
      command.setCommitHook((workDv) -> {
         PsqlDeleteWorkTask task = new PsqlDeleteWorkTask(workDv);

         WorkChangeNotifier<String> workChangeNotifier = new WorkChangeNotifier<>(workDv.id, ChangeType.DELETED);
         ObservableTaskWrapper<String> wrapTask = new ObservableTaskWrapper<String>(task, workChangeNotifier);

         Future<String> submitWork = exec.submit(wrapTask);
         return submitWork;
      });

      return command;
   }

   private void notifyRelationshipUpdate(ChangeType type, String relnId)
   {
      WorksChangeEventImpl evt = new WorksChangeEventImpl(type, relnId);
      listeners.forEach(ears -> {
         notifications.submit(() -> {
            try {
               ears.accept(evt);
            } catch (Exception ex) {
               logger.log(Level.WARNING, "Call to update listener failed.", ex);
            }
         });
      });
   }

   @Override
   public AutoCloseable addBeforeUpdateListener(Consumer<WorksChangeEvent> ears)
   {
      throw new UnsupportedOperationException("not impl");
   }

   @Override
   public AutoCloseable addAfterUpdateListener(Consumer<WorksChangeEvent> ears)
   {
      listeners.add(ears);
      return () -> listeners.remove(ears);
   }

   private class WorksChangeEventImpl implements WorksChangeEvent
   {
      private final ChangeType type;
      private final String id;

      public WorksChangeEventImpl(ChangeType type, String id)
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
      public String getWorkId()
      {
         return id;
      }

      @Override
      public Work getWorkEvt() throws WorkNotAvailableException
      {
         try
         {
            return getWork(id);
         }
         catch (NoSuchCatalogRecordException e)
         {
            throw new WorkNotAvailableException("Internal error occured while retrieving work [" + id + "]");
         }
      }

      @Override
      public String toString()
      {
         return "Relationship Change Event: action = " + type + "; id = " + id;
      }

   }

   // FIXME What is ResultType .. surely you know what this is?
   //       Note that you should be using the adapter, not the observer. You aren't maintaining
   //       the internal state as required by the update observer API.
   private final class WorkChangeNotifier<ResultType> implements DataUpdateObserver<ResultType>
   {
      private final String id;
      private final ChangeType type;

      public WorkChangeNotifier(String id, ChangeType type)
      {
         this.id = id;
         this.type = type;

      }

      @Override
      public boolean start()
      {
         return true;
      }

      @Override
      public void finish(ResultType result)
      {
         notifyRelationshipUpdate(type, id);
      }

      @Override
      public void aborted()
      {
         // no-op
      }

      @Override
      public void error(String message, Exception ex)
      {
         // no-op
      }

      @Override
      public boolean isCanceled()
      {
         return false;
      }

      @Override
      public boolean isCompleted()
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public State getState()
      {
         throw new UnsupportedOperationException();
      }
   }

   /**
    * @param work
    * @return Context for generating IDs for Editions within a Work.
    */
   public static String getContext(WorkDV work)
   {
      return WORK_CONTEXT + "/" + work.id;
   }

   /**
    * @param work
    * @param edition
    * @return Context for generating IDs for Volumes within an Edition (subs. w/in a Work).
    */
   public static String getContext(WorkDV work, EditionDV edition)
   {
      return getContext(work) + "/" + edition.id;
   }
}
