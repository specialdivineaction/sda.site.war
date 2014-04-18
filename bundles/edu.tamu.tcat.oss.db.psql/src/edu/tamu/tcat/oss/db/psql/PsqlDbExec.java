package edu.tamu.tcat.oss.db.psql;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.db.DbExecutor;

public class PsqlDbExec implements DbExecutor, AutoCloseable
{
   public static final Logger DB_LOGGER = Logger.getLogger("edu.tamu.tcat.oss.db.hsqldb");

   private ExecutorService executor;

   private DataSource provider;

   public PsqlDbExec(DataSource provider)
   {
      this.provider = provider;
   }

   @Override
   public void close() 
   {
      boolean terminated = false;
      try
      {
         executor.shutdown();
         terminated = executor.awaitTermination(30, TimeUnit.SECONDS);
      }
      catch (InterruptedException e)
      {
         terminated = false;
      }
      
      if (!terminated)
      {
         DB_LOGGER.log(Level.SEVERE, "DBExecutor failed to complete all tasks.");
         executor.shutdownNow();
      }
   }

   @Override
   @SuppressWarnings("unchecked")            // FIXME this seems bad.
   public Future<?> submit(DbExecTask task)
   {
      ExecutionTaskRunner runner = new ExecutionTaskRunner(provider, task);

      // TODO to allow cancellation, timeouts, etc, should grab returned future.
      return executor.submit(runner);
   }

 
   private static class ExecutionTaskRunner implements Runnable
   {
      private final DbExecTask task;
      private final DataSource provider;

      ExecutionTaskRunner(DataSource provider, DbExecTask task)
      {
         this.provider = provider;
         this.task = task;
      }

      @Override
      public void run()
      {
         try (Connection conn = provider.getConnection())
         {
            task.setConnection(conn);
            task.run();
         }
         catch (Exception ex)
         {
            DB_LOGGER.log(Level.SEVERE, "Database task execution failed.", ex);
         }
      }
   }

}
