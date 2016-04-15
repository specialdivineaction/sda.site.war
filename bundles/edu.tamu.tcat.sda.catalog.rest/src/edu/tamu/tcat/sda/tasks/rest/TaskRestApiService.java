package edu.tamu.tcat.sda.tasks.rest;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.dcopies.AssignCopiesEditorialTask;
import edu.tamu.tcat.sda.tasks.rest.v1.TaskCollectionResource;
import edu.tamu.tcat.trc.repo.postgres.id.UuidProvider;

@Path("/")
public class TaskRestApiService
{
   private static final Logger logger = Logger.getLogger(TaskRestApiService.class.getName());

   private SqlExecutor sqlExecutor;
   private ExecutorService executorService;
//   private WorkflowManager workflowManager;

   // HACK should come from somewhere
   private AssignCopiesEditorialTask editorialTask;


   public void setSqlExecutor(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = sqlExecutor;
   }

//   public void setWorkflowManager(WorkflowManager workflowManager)
//   {
//      this.workflowManager = workflowManager;
//   }

   public void activate()
   {
      Objects.requireNonNull(sqlExecutor, "No SQL Executor provided");
//      Objects.requireNonNull(workflowManager, "No Workflow Manager provided");

      executorService = Executors.newCachedThreadPool();

      editorialTask = new AssignCopiesEditorialTask(sqlExecutor, new UuidProvider(), executorService);
   }

   public void dispose()
   {
      sqlExecutor = null;
//      workflowManager = null;

      editorialTask = null;

      executorService.shutdown();
      try
      {
         // HACK: wait for an arbitrary time interval
         executorService.awaitTermination(10, TimeUnit.SECONDS);
      }
      catch (InterruptedException e)
      {
         logger.log(Level.SEVERE, "Failed to stop executor service in a timely manner.", e);
      }
      finally
      {
         executorService.shutdownNow();
         executorService = null;
      }
   }

   // HACK this implies that task param cannot be "workflow"
   @Path("/v1/tasks")
   public TaskCollectionResource getTaskCollectionResource()
   {
      return new TaskCollectionResource(editorialTask);
   }

}
