package edu.tamu.tcat.sda.tasks.rest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.TaskSubmissionMonitor;
import edu.tamu.tcat.sda.tasks.impl.AssignCopiesEditorialTask;
import edu.tamu.tcat.sda.tasks.impl.AssignRelationshipsEditorialTask;
import edu.tamu.tcat.sda.tasks.rest.v1.TaskCollectionResource;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;

@Path("/")
public class TaskRestApiService
{
   private static final Logger logger = Logger.getLogger(TaskRestApiService.class.getName());

   private SqlExecutor sqlExecutor;
   private ExecutorService executorService;

   private final Map<String, EditorialTask<?>> tasks = new HashMap<>();

   private EntryRepositoryRegistry repoRegistry;

   public void setSqlExecutor(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = sqlExecutor;
   }

   public void setRepoRegistry(EntryRepositoryRegistry repoRegistry)
   {
      this.repoRegistry = repoRegistry;
   }

   public void activate()
   {
      try
      {
         logger.info(() -> "Activating " + getClass().getSimpleName());
         Objects.requireNonNull(sqlExecutor, "No SQL Executor provided");

         executorService = Executors.newCachedThreadPool();

         // HACK: hard-coded tasks
         Stream.of(
               new AssignCopiesEditorialTask("copies", sqlExecutor, () -> UUID.randomUUID().toString(), executorService),
               new AssignRelationshipsEditorialTask("relns", sqlExecutor, () -> UUID.randomUUID().toString(), executorService)
            ).forEach(t -> tasks.put(t.getId(), t));
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to start task REST API service.", e);
      }
   }

   public void dispose()
   {
      tasks.clear();
      sqlExecutor = null;

      try
      {
         executorService.shutdown();
         executorService.awaitTermination(10, TimeUnit.SECONDS);
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to stop executor service in a timely manner.", e);
      }
      finally
      {
         executorService.shutdownNow();
         executorService = null;
      }
   }

   @Path("/v1/tasks")
   public TaskCollectionResource getTaskCollectionResource()
   {
      return new TaskCollectionResource(tasks);
   }

   @POST
   @Path("/v1/tasks/{id}/init")
   @Produces(MediaType.TEXT_PLAIN)
   public StreamingOutput addWorkItem(@PathParam("id") String taskId)
   {
      // HACK Not sure how to safely convert from EditorialTask<?> to EditorialTask<Work>
      //      This entire method is just a hack to prepopulate a task anyway.
      EditorialTask<BibliographicEntry> task = (EditorialTask<BibliographicEntry>)tasks.get(taskId);

      return (os) -> {
         Writer out = new BufferedWriter(new OutputStreamWriter(os));
         BibliographicEntryRepository workRepository = repoRegistry.getRepository(null, BibliographicEntryRepository.class);
         Iterator<BibliographicEntry> workIterator = workRepository.listAll();
         Supplier<BibliographicEntry> workSupplier = () -> workIterator.hasNext() ? workIterator.next() : null;
         WorkTaskSubmissionMonitor monitor = new WorkTaskSubmissionMonitor(out, task.getName());
         task.addItems(workSupplier, monitor);
         try
         {
            monitor.awaitFinished(5, TimeUnit.MINUTES);
         }
         catch (InterruptedException e)
         {
            logger.log(Level.WARNING, "Monitor interrupted while waiting for 'finished'.", e);
         }
      };
   }

   private static class WorkTaskSubmissionMonitor implements TaskSubmissionMonitor
   {
      private static final Logger logger = Logger.getLogger(WorkTaskSubmissionMonitor.class.getName());

      private final Writer output;
      private final String taskName;

      private final AtomicInteger successCount = new AtomicInteger(0);
      private final AtomicInteger failureCount = new AtomicInteger(0);

      private final CountDownLatch finished = new CountDownLatch(1);

      public WorkTaskSubmissionMonitor(Writer output, String taskName)
      {
         this.output = output;
         this.taskName = taskName;
      }

      @Override
      public void finished()
      {
         int numSuccesses = successCount.get();
         int numFailures = failureCount.get();

         String message = MessageFormat.format("Finished! Added {0} item{1} to the \"{2}\" task. {3} error{4} reported.",
               numSuccesses,
               numSuccesses == 1 ? "" : "s",
               taskName,
               numFailures,
               numFailures == 1 ? "" : "s");

         synchronized (output) {
            try
            {
               output.write(message);
               output.flush();
               output.close();
            }
            catch (IOException e)
            {
               logger.log(Level.WARNING, "Failed to send message: " + message, e);
            }
         }

         finished.countDown();
      }

      @Override
      public <EntityType> void failed(WorkItemCreationError<EntityType> error)
      {
         failureCount.incrementAndGet();
         EntityType entity = error.getEntity();
         String message = MessageFormat.format("Failed to add entity '{0}'.", entity.toString());

         logger.log(Level.WARNING, message, error.getException());

         synchronized (output) {
            try
            {
               output.write(message);
            }
            catch (IOException e)
            {
               logger.log(Level.WARNING, "Failed to send message: " + message, e);
            }
         }
      }

      @Override
      public <EntityType> void created(WorkItemCreationRecord<EntityType> record)
      {
         successCount.incrementAndGet();
      }

      public void awaitFinished(long timeout, TimeUnit unit) throws InterruptedException
      {
         finished.await(timeout, unit);
      }
   }

}
