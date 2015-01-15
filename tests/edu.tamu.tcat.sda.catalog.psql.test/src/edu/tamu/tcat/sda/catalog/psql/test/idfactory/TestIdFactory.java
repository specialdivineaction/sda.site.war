package edu.tamu.tcat.sda.catalog.psql.test.idfactory;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.idfactory.impl.db.FilePersistenceStrategy;
import edu.tamu.tcat.sda.catalog.idfactory.impl.db.IdFactoryImpl;

public class TestIdFactory
{
   public static final Path TEST_PERSIST_PATH = Paths.get("/tmp/idfactory.json");
   private IdFactoryImpl factory;
   private Map<String, Long> startIds = new HashMap<>();

   @Before
   public void setUp() throws Exception
   {

      JacksonJsonMapper mapper = new JacksonJsonMapper();
      mapper.activate();

      // attempt to read IDs from file
      if (TEST_PERSIST_PATH.toFile().exists()) {
         try {
            @SuppressWarnings("unchecked")
            Map<String, String> raw = mapper.parse(Files.newInputStream(TEST_PERSIST_PATH, StandardOpenOption.READ), Map.class);
            raw.forEach((k,v) -> startIds.put(k, Long.valueOf(v)));
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }

      factory = new IdFactoryImpl();
      factory.setMapper(mapper);
      factory.setConfiguration(new ConfigurationProperties()
      {
         private Map<String, Object> properties = new HashMap<>();

         {
            properties.put(FilePersistenceStrategy.CONFIG_PERSIST_PATH, TEST_PERSIST_PATH);
         }

         @Override
         public <T> T getPropertyValue(String name, Class<T> type, T defaultValue) throws IllegalStateException
         {
            if (!properties.containsKey(name)) {
               return defaultValue;
            }

            Object val = properties.get(name);

            if (type.isAssignableFrom(val.getClass())) {
               return type.cast(val);
            } else {
               throw new IllegalStateException("Value type [" + val.getClass().getName() + "] does not match expected type [" + type.getName() + "].");
            }
         }

         @Override
         public <T> T getPropertyValue(String name, Class<T> type) throws IllegalStateException
         {
            return getPropertyValue(name, type, null);
         }
      });

      factory.activate();
   }

   @After
   public void tearDown() throws Exception
   {
      factory.dispose();
   }

   @Test
   public void testGetNextId()
   {
      String rootContext = "works";
      long workId = startIds.getOrDefault(rootContext, Long.valueOf(1)).longValue();
      for (long i = 0; i < 10; i++) {
         String expectedWorkId = String.valueOf(workId + i);
         String actualWorkId = factory.getNextId(rootContext);
         assertEquals(expectedWorkId, actualWorkId);

         String workContext = rootContext + '/' + expectedWorkId;
         long editionId = startIds.getOrDefault(workContext, Long.valueOf(1)).longValue();
         for (long j = 0; j < 10; j++) {
            String expectedEditionId = String.valueOf(editionId + j);
            String actualEditionId = factory.getNextId(workContext);
            assertEquals(expectedEditionId, actualEditionId);

            String editionContext = workContext + '/' + expectedEditionId;
            long volumeId = startIds.getOrDefault(editionContext, Long.valueOf(1)).longValue();
            for (long k = 0; k < 10; k++) {
               String expectedVolumeId = String.valueOf(volumeId + k);
               String actualVolumeId = factory.getNextId(editionContext);
               assertEquals(expectedVolumeId, actualVolumeId);
            }
         }
      }
   }
}
