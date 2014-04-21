package edu.tamu.tcat.oss.db.psql;

import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
      executor = Executors.newSingleThreadExecutor();
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
   public <T> Future<T> submit(DbExecTask<T> task)
   {
      // TODO to allow cancellation, timeouts, etc, should grab returned future.
      ExecutionTaskRunner<T> runner = new ExecutionTaskRunner<T>(provider, task);
      return executor.submit(runner);
   }

   private static class ExecutionTaskRunner<T> implements Callable<T>
   {
      private final DbExecTask<T> task;
      private final DataSource provider;

      ExecutionTaskRunner(DataSource provider, DbExecTask<T> task)
      {
         this.provider = provider;
         this.task = task;
      }

      @Override
      public T call() throws Exception
      {
         try (Connection conn = provider.getConnection())
         {
            return task.execute(conn);
         }
      }
   }

}
