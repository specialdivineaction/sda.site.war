package edu.tamu.tcat.trc.entries.types.biblio.test;

import java.net.URI;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.postgresql.exec.PostgreSqlExecutor;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.sda.catalog.psql.provider.PsqlDataSourceProvider;
import edu.tamu.tcat.trc.entries.bib.UpdateCanceledException;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReference;
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
      repo.activate();
   }

   @After
   public void tearDownTest() throws InterruptedException, ExecutionException
   {
      String sql = "DELETE FROM copy_reference WHERE reference->>'associatedEntry' LIKE 'test%'";
      Future<Void> future = exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return null;
         }
      });

      future.get();

      repo.dispose();
      exec.close();
      dsp.dispose();
      config.dispose();
   }

   @Test
   public void testReferenceCreation() throws UpdateCanceledException, InterruptedException, ExecutionException
   {
      EditCopyReferenceCommand editor = repo.create();
      editor.setAssociatedEntry(URI.create("test/works/1"));
      String id = "htid:000000000#ark+=13960=t00z72x8w";
      editor.setCopyId(id);
      editor.setTitle("Copy from my harddrive");
      editor.setSummary("A copy reference example.");
      editor.setRights("full view");

      Future<CopyReference> future = editor.execute();
      CopyReference ref = future.get();
   }
}
