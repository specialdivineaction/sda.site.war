package edu.tamu.tcat.sda.catalog.psql.idfactory.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.idfactory.IdFactory;

/**
 * An arbiter to hand out monotonically increasing numeric IDs unique to a particular (String)
 * context.
 *
 * Twitter has encountered a similar need for generating coherent IDs and has developed Snowflake:
 * https://blog.twitter.com/2010/announcing-snowflake
 */
/**
 * @author matt.barry
 *
 */
/**
 * @author matt.barry
 *
 */
/**
 * @author matt.barry
 *
 */
/**
 * @author matt.barry
 *
 */
public class IdFactoryImpl implements IdFactory
{
   private static final String PERSIST_FILE_CONFIG_PROPERTY = "idfactory.persistence.filepath";

   private static final long PERSIST_INTERVAL_PERIOD = 5;
   private static final TimeUnit PERSIST_INTERVAL_PERIOD_UNIT = TimeUnit.MINUTES;

   private static final long PERSIST_SHUTDOWN_DELAY = 10;
   private static final TimeUnit PERSIST_SHUTDOWN_DELAY_UNIT = TimeUnit.SECONDS;

   private static final Logger logger = Logger.getLogger(IdFactoryImpl.class.getName());

   /**
    * Stores a mapping of context to the <em>next</em> ID in the system.
    */
   private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
   private ConfigurationProperties config;
   private JsonMapper mapper;
   private ScheduledExecutorService executor;
   private AtomicBoolean isDirty = new AtomicBoolean(false);
   private ScheduledFuture<?> persistTaskHandle;


   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setMapper(JsonMapper mapper)
   {
      this.mapper = mapper;
   }

   public void activate()
   {
      // load saved state from file on disk
      Path filePath = config.getPropertyValue(PERSIST_FILE_CONFIG_PROPERTY, Path.class);

      try (InputStream in = Files.newInputStream(filePath, StandardOpenOption.READ)) {
         // Type erasure prevents us from passing generic parameters to mapper.parse
         // We just have to trust that the values have been saved correctly
         @SuppressWarnings("unchecked")
         Map<String, String> initialValues = mapper.parse(in, Map.class);

         initialValues.forEach((k,v) -> counters.put(k, new AtomicLong(Long.parseLong(v))));
      }
      catch (IOException e) {
         if (filePath.toFile().exists()) {
            throw new IllegalStateException("Unable to read IdFactory state file", e);
         }
      }
      catch (JsonException | ClassCastException | NumberFormatException e) {
         throw new IllegalStateException("Malformed IdFactory file", e);
      }

      // start polling for changes to be saved
      executor = Executors.newScheduledThreadPool(1);
      persistTaskHandle = executor.scheduleAtFixedRate(this::persistCounters, 0, PERSIST_INTERVAL_PERIOD, PERSIST_INTERVAL_PERIOD_UNIT);
   }

   public void dispose()
   {
      // stop polling for changes
      persistTaskHandle.cancel(false);

      try {
         if (!executor.awaitTermination(PERSIST_SHUTDOWN_DELAY, PERSIST_SHUTDOWN_DELAY_UNIT)) {
            logger.log(Level.INFO, "Periodic persistence task failed to shut down in a timely manner... Requesting a little more urgently...");
            executor.shutdownNow();
         }
      }
      catch (InterruptedException e) {
         logger.log(Level.WARNING, "Periodic persistence shutdown interrupted", e);
      }

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
            Path filePath = config.getPropertyValue(PERSIST_FILE_CONFIG_PROPERTY, Path.class);
            try (OutputStream out = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
               Map<String, String> exportMap = counters.entrySet().parallelStream()
                     .collect(Collectors.toMap(Map.Entry::getKey, (e) -> e.getValue().toString()));

               String json = mapper.asString(exportMap);
               out.write(json.getBytes());
            }
            catch (IOException e) {
               throw new Exception("Unable to open or create IdFactory state file", e);
            }
            catch (JsonException e) {
               throw new Exception("Unable to save IdFatory state", e);
            }

            logger.log(Level.INFO, "Periodic IdFactory persist completed successfully");
         }
      }
      catch (Exception e) {
         logger.log(Level.WARNING, "Encountered error while running periodic IdFactory persist", e);
      }
   }
}
