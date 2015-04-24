package edu.tamu.tcat.trc.entries.types.biblio.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.postgresql.exec.PostgreSqlExecutor;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.sda.catalog.psql.provider.PsqlDataSourceProvider;
import edu.tamu.tcat.trc.entries.bib.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.entries.bib.copies.postgres.PsqlDigitalCopyLinkRepo;

public class CopyRepoTest
{

   private PostgreSqlExecutor exec;
   private SimpleFileConfigurationProperties config;
   private PsqlDataSourceProvider dsp;
   private PsqlDigitalCopyLinkRepo repo;

   @BeforeClass
   public static void setUp()
   {
      // TODO spin up DB, etc
   }

   @AfterClass
   public static void tearDown()
   {

   }

   @Before
   public void setupTest() throws DataSourceException
   {
      Map<String, Object> params = new HashMap<>();
      params.put(SimpleFileConfigurationProperties.PROP_FILE, "config.path");
      config = new SimpleFileConfigurationProperties();
      config.activate(params);

      dsp = new PsqlDataSourceProvider();
      dsp.bind(config);
      dsp.activate();

      exec = new PostgreSqlExecutor();
      exec.init(dsp);

      repo = new PsqlDigitalCopyLinkRepo();
      repo.setDatabaseExecutor(exec);
   }

   @After
   public void tearDownTest()
   {
      repo.dispose();
      exec.close();
      dsp.dispose();
      config.dispose();
   }

   @Test
   public void doSomething()
   {
      EditCopyReferenceCommand create = repo.create();
   }
}
