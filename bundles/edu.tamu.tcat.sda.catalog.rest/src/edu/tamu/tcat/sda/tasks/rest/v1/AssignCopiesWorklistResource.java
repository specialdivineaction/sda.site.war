package edu.tamu.tcat.sda.tasks.rest.v1;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import edu.tamu.tcat.sda.tasks.PartialWorkItemSet;
import edu.tamu.tcat.sda.tasks.TaskSubmissionMonitor;
import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.dcopies.AssignCopiesEditorialTask;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;

/**
 *  Implements the REST API for the list of work items associated with a particular
 *  taks. This API is scoped to a particular user account so that the returned items reflect
 *  that account's view of the work to be performed within a task.
 */
public class AssignCopiesWorklistResource
{
   private static final Logger logger = Logger.getLogger(AssignCopiesWorklistResource.class.getName());

   private final AssignCopiesEditorialTask task;
   private WorkRepository workRepository;

   public AssignCopiesWorklistResource(AssignCopiesEditorialTask task, WorkRepository workRepository)
   {
      this.task = task;
      this.workRepository = workRepository;
   }

   @Path("{id}")
   public AssignCopiesWorkItemResource getItem(@PathParam("id") String id)
   {
      WorkItem item = task.getItem(id);
      return new AssignCopiesWorkItemResource(task, item);
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorklistGroup listItems(@QueryParam("stage") String stageId,
                                            @QueryParam("start") @DefaultValue("0") int start,
                                            @QueryParam("max") @DefaultValue("10") int max)
   {
      Workflow workflow = task.getWorkflow();
      Map<String, WorkflowStage> stages = workflow.getStages().stream()
            .collect(Collectors.toMap(WorkflowStage::getId, o -> o));

      if (!stages.containsKey(stageId))
      {
         String message = MessageFormat.format("Unknown stage ID '{0}'.", stageId);
         throw new BadRequestException(message);
      }

      WorkflowStage stage = stages.get(stageId);
      PartialWorkItemSet itemSet = task.getItems(stage, start, max);
      return RepoAdapter.makeWorklistGroup(task, stage, itemSet);
   }

   @POST
//   @Consumes(MediaType.APPLICATION_JSON)
//   @Produces(MediaType.APPLICATION_JSON)
//   public RestApiV1.WorkItem addWorkItem(RestApiV1.WorkItem item)
   @Produces(MediaType.TEXT_PLAIN)
   public StreamingOutput addWorkItem()
   {
      // HACK no better place to put this...
      return (os) -> {
         Writer out = new BufferedWriter(new OutputStreamWriter(os));
         Iterator<Work> workIterator = workRepository.getAllWorks();
         Supplier<Work> workSupplier = () -> workIterator.hasNext() ? workIterator.next() : null;
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
