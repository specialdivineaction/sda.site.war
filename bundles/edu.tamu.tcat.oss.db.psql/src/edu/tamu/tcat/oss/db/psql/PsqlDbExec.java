package edu.tamu.tcat.oss.db.psql;

import java.sql.Connection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.postgresql.Driver;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.db.psql.internal.BasicDataSourceExt;
import edu.tamu.tcat.oss.osgi.config.ConfigurationProperties;

public class PsqlDbExec implements DbExecutor, AutoCloseable
{
   public static final Logger DB_LOGGER = Logger.getLogger("edu.tamu.tcat.oss.db.hsqldb");

   public static final String PROP_URL = "db.postgres.url";
   public static final String PROP_USER = "db.postgres.user";
   public static final String PROP_PASS = "db.postgres.pass";
   
   public static final String PROP_MAX_ACTIVE = "db.postgres.active.max";
   public static final String PROP_MAX_IDLE = "db.postgres.idle.max";
   public static final String PROP_MIN_IDLE = "db.postgres.idle.min";
   public static final String PROP_MIN_EVICTION = "db.postgres.eviction.min";
   public static final String PROP_BETWEEN_EVICTION = "db.postgres.eviction.between";
   
   private ExecutorService executor;
   private DataSource provider;
   private ConfigurationProperties props;

   public PsqlDbExec()
   {
   }
   
   public void setConfigurationProperties(ConfigurationProperties props)
   {
      this.props = props;
   }
   
   private static int getIntValue(ConfigurationProperties props, String prop, int defaultValue)
   {
      Integer d = Integer.valueOf(defaultValue);
      Integer result = props.getPropertyValue(prop, Integer.class, d);
      
      return result.intValue();
   }
   
   // called by OSGi DS
   public void activate()
   {
      Objects.requireNonNull(props, "Configuration properties not supplied");
      
      String url = props.getPropertyValue(PROP_URL, String.class);
      String user = props.getPropertyValue(PROP_USER, String.class);
      String pass = props.getPropertyValue(PROP_PASS, String.class);
      
      Objects.requireNonNull(url, "Database connection URL not supplied");
      Objects.requireNonNull(url, "Database username not supplied");
      Objects.requireNonNull(url, "Database password not supplied");

      int maxActive = getIntValue(props, PROP_MAX_ACTIVE, 30);
      int maxIdle = getIntValue(props, PROP_MAX_IDLE, 3);
      int minIdle = getIntValue(props, PROP_MIN_IDLE, 0);
      int minEviction = getIntValue(props, PROP_MIN_EVICTION, 10 * 1000);
      int betweenEviction = getIntValue(props, PROP_BETWEEN_EVICTION, 100);

      Driver driver = new org.postgresql.Driver();
      BasicDataSource dataSource = new BasicDataSourceExt(driver);

      dataSource.setDriverClassName(driver.getClass().getName());
      dataSource.setUsername(user);
      dataSource.setPassword(pass);
      dataSource.setUrl(url);

      dataSource.setMaxActive(maxActive);
      dataSource.setMaxIdle(maxIdle);
      dataSource.setMinIdle(minIdle);
      dataSource.setMinEvictableIdleTimeMillis(minEviction);
      dataSource.setTimeBetweenEvictionRunsMillis(betweenEviction);

      this.executor = Executors.newSingleThreadExecutor();
      this.provider = dataSource;
   }
   
   public void dispose()
   {
      this.close();
   }

   @Override
   public void close() 
   {
      if (executor != null)
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

      private Connection getConnection() throws Exception
      {
         try 
         {
            return provider.getConnection();
         } 
         catch (Exception ex)
         {
            DB_LOGGER.log(Level.SEVERE, "Failed to obtain database connection", ex);
            throw ex;
            
         }
      }
      
      @Override
      public T call() throws Exception
      {
         try (Connection conn = getConnection())
         {
            return task.execute(conn);
         }
      }
   }
}
