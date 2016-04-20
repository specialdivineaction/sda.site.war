package edu.tamu.tcat.sda.tasks.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.dcopies.AssignCopiesEditorialTask;
import edu.tamu.tcat.sda.tasks.rest.v1.AssignCopiesTaskCollectionResource;
import edu.tamu.tcat.trc.repo.postgres.id.UuidProvider;

@Path("/")
public class TaskRestApiService
{
   private static final Logger logger = Logger.getLogger(TaskRestApiService.class.getName());

   private SqlExecutor sqlExecutor;
   private ExecutorService executorService;

   // TODO need to provide a configurable way to look up tasks.
   private final Map<String, AssignCopiesEditorialTask> tasks = new HashMap<>();

   public void setSqlExecutor(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = sqlExecutor;
   }

   public void activate()
   {
      Objects.requireNonNull(sqlExecutor, "No SQL Executor provided");

      executorService = Executors.newCachedThreadPool();
      AssignCopiesEditorialTask task = new AssignCopiesEditorialTask(sqlExecutor, new UuidProvider(), executorService);
      tasks.put(task.getId(), task);
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
   public AssignCopiesTaskCollectionResource getTaskCollectionResource()
   {
      return new AssignCopiesTaskCollectionResource(tasks, workRepository);
   }

}
