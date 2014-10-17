package edu.tamu.tcat.sda.catalog.psql.idfactory.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.idfactory.PersistenceException;
import edu.tamu.tcat.sda.catalog.psql.idfactory.PersistenceStrategy;

public class FilePersistenceStrategy implements PersistenceStrategy
{
   private static final String CONFIG_PERSIST_PATH = "idfactory.persist.filepath";

   private ConfigurationProperties config;
   private JsonMapper mapper;


   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setMapper(JsonMapper mapper)
   {
      this.mapper = mapper;
   }

   @Override
   public void save(Map<String, String> data) throws PersistenceException
   {
      Path filePath = config.getPropertyValue(CONFIG_PERSIST_PATH, Path.class);
      try (OutputStream out = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
         String json = mapper.asString(data);
         out.write(json.getBytes());
      }
      catch (IOException e) {
         throw new PersistenceException("Unable to open or create IdFactory state file", e);
      }
      catch (JsonException e) {
         throw new PersistenceException("Unable to save IdFatory state", e);
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public Map<String, String> load() throws PersistenceException
   {
      // load saved state from file on disk
      Path filePath = config.getPropertyValue(CONFIG_PERSIST_PATH, Path.class);

      Map<String, String> rv = null;

      try (InputStream in = Files.newInputStream(filePath, StandardOpenOption.READ)) {
         // Type erasure prevents us from passing generic parameters to mapper.parse
         // We just have to trust that the values have been saved correctly
         rv = mapper.parse(in, Map.class);
      }
      catch (IOException e) {
         if (filePath.toFile().exists()) {
            throw new PersistenceException("Unable to read IdFactory state file", e);
         }
      }
      catch (JsonException | ClassCastException | NumberFormatException e) {
         throw new PersistenceException("Malformed IdFactory file", e);
      }

      return rv;
   }

}
