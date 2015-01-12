package edu.tamu.tcat.sda.catalog.psql.idfactory.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.IdFactory;
import edu.tamu.tcat.sda.catalog.psql.idfactory.PersistenceException;

/**
 * An arbiter to hand out monotonically increasing numeric IDs unique to a particular (String)
 * context.
 *
 * Twitter has encountered a similar need for generating coherent IDs and has developed Snowflake:
 * https://blog.twitter.com/2010/announcing-snowflake
 */
@Deprecated
public class IdFactoryImpl implements IdFactory
{
   public static final String CFG_PERSIST_INTERVAL = "idfactory.persist.interval";
   public static final String CFG_PERSIST_SHUTDOWN_DELAY = "idfactory.persist.shutdown.delay";

   private static final Long PERSIST_INTERVAL_DEFAULT = Long.valueOf(300);
   private static final Long PERSIST_SHUTDOWN_DELAY_DEFAULT = Long.valueOf(10);

   private static final Logger logger = Logger.getLogger(IdFactoryImpl.class.getName());

   /**
    * Stores a mapping of context to the <em>next</em> ID in the system.
    */
   private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

   private ConfigurationProperties config;
   private ScheduledExecutorService executor;

   // TODO: this should be a service
   private FilePersistenceStrategy persistenceStrategy = new FilePersistenceStrategy();

   /**
    * Whether the internal state has changed since the last save operation.
    */
   private AtomicBoolean isDirty = new AtomicBoolean(false);

   /**
    * A handle by which to cancel the periodic task that persists state to disk.
    */
   private ScheduledFuture<?> persistTaskHandle;


   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
      this.persistenceStrategy.setConfiguration(config);
   }

   public void setMapper(JsonMapper mapper)
   {
      this.persistenceStrategy.setMapper(mapper);
   }

   public void activate()
   {
      try {
         Map<String, String> initialValues = persistenceStrategy.load();
         if (initialValues != null) {
            initialValues.forEach((k,v) -> counters.put(k, new AtomicLong(Long.parseLong(v))));
         }
      }
      catch (PersistenceException e) {
         throw new IllegalStateException("Unable to load saved state", e);
      }

      Long interval = config.getPropertyValue(CFG_PERSIST_INTERVAL, Long.class, PERSIST_INTERVAL_DEFAULT);

      // start polling for changes to be saved
      executor = Executors.newScheduledThreadPool(1);
      persistTaskHandle = executor.scheduleAtFixedRate(this::persistCounters, 0, interval.longValue(), TimeUnit.SECONDS);
   }

   public void dispose()
   {
      // stop polling for changes
      persistTaskHandle.cancel(false);

      Long delay = config.getPropertyValue(CFG_PERSIST_SHUTDOWN_DELAY, Long.class, PERSIST_SHUTDOWN_DELAY_DEFAULT);

      try {
         executor.shutdown();
         if (!executor.awaitTermination(delay.longValue(), TimeUnit.SECONDS)) {
            logger.log(Level.INFO, "Periodic persistence task failed to shut down in a timely manner... Requesting a little more urgently...");
            executor.shutdownNow();
         }
      }
      catch (InterruptedException e) {
         logger.log(Level.WARNING, "Periodic persistence shutdown interrupted", e);
         executor.shutdownNow();
      }

      // "deallocate" executor
      executor = null;

      // save one last time for good measure
      persistCounters();
   }

   @Override
   public String getNextId(String context)
   {
      counters.putIfAbsent(context, new AtomicLong(1));
      long id = counters.get(context).getAndIncrement();

      isDirty.set(true);

      return String.valueOf(id);
   }



   /**
    * Persist internal state to disk. Automatically executed periodically by executor.
    */
   private synchronized void persistCounters()
   {
      try {
         if (isDirty.getAndSet(false)) {
            Map<String, String> exportMap = counters.entrySet().parallelStream()
                  .collect(Collectors.toMap(Map.Entry::getKey, (e) -> e.getValue().toString()));

            persistenceStrategy.save(exportMap);

            logger.log(Level.INFO, "Periodic IdFactory persist completed successfully");
         }
      }
      catch (Exception e) {
         logger.log(Level.WARNING, "Encountered error while running periodic IdFactory persist", e);
      }
   }
}
