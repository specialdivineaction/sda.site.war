package edu.tamu.tcat.oss.db.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
   
   private Connection getConnection()
   {
      // FIXME make this configurable
      Connection con = null;

      String url = "jdbc:postgresql://localhost:5433/SDA";
      String user = "postgres";
      String password = "";

      try
      {

         Class.forName("org.postgresql.Driver");
         con = DriverManager.getConnection(url, user, password);

      }
      catch (SQLException | ClassNotFoundException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return con;
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
