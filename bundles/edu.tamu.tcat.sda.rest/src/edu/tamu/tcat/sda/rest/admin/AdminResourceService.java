package edu.tamu.tcat.sda.rest.admin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.BibliographicSearchStrategy;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.IndexAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.BioSearchStrategy;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.SolrDocAdapter;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.impl.search.RelnDocument;
import edu.tamu.tcat.trc.entries.types.reln.impl.search.RelnSearchStrategy;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.search.solr.BasicSearchSvcMgr;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

@Path("/admin")
public class AdminResourceService
{
   private static final Logger logger = Logger.getLogger(AdminResourceService.class.getName());

   // TODO we are creating a new SolrClients here instead of using existing index services because
   //      they do not allow for batch indexing and purging.
   // TODO remove dependency on solrj from MANIFEST.MF once HttpSolrClient is removed

   private ConfigurationProperties config;
   private EntryRepositoryRegistry repoRegistry;

   public void setRepoRegistry(EntryRepositoryRegistry repoRegistry)
   {
      this.repoRegistry = repoRegistry;
   }

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      try
      {
         logger.info(() -> "Activating " + getClass().getSimpleName());

         Objects.requireNonNull(config, "No Configuration supplied.");
         Objects.requireNonNull(repoRegistry, "No work repository supplied.");
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to activate admin REST resource service.", e);
         throw e;
      }
   }

   public void deactivate()
   {
      repoRegistry = null;
      config = null;
   }

   private void withSolrClient(String coreId, Consumer<SolrClient> consumer)
   {
      URI solrBaseUri = config.getPropertyValue(BasicSearchSvcMgr.SOLR_API_ENDPOINT, URI.class);
      String solrCoreProperty = MessageFormat.format(BasicSearchSvcMgr.SOLR_CORE_ID, coreId);
      String solrCore = config.getPropertyValue(solrCoreProperty, String.class);
      URI solrCoreUri = solrBaseUri.resolve(solrCore);
      try (SolrClient client = new HttpSolrClient(solrCoreUri.toString()))
      {
         consumer.accept(client);
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, MessageFormat.format("Encountered problem while attempting to access {0} solr core.", coreId), e);
      }
   }

   @GET
   @Produces(MediaType.TEXT_PLAIN)
   public String ping()
   {
      return "Hello, world!";
   }

   @POST
   @Path("/reindex/people")
   public void reindexPeople()
   {
      // purge all existing records
      withSolrClient(BioSearchStrategy.SOLR_CORE, solr -> {
         try
         {
            solr.deleteByQuery("*:*");
            solr.commit();
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Failed to remove data from the people core.", e);
         }

         BiographicalEntryRepository peopleRepository = repoRegistry.getRepository(null, BiographicalEntryRepository.class);

         Iterable<BiographicalEntry> people = () -> peopleRepository.listAll();

         SolrDocAdapter adapter = new SolrDocAdapter(this::extractFirstSentence);

         Collection<SolrInputDocument> solrDocs = StreamSupport.stream(people.spliterator(), false)
               .map(adapter::apply)
               .filter(Objects::nonNull)
               .collect(Collectors.toList());

         try
         {
            solr.add(solrDocs);
            solr.commit();
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Failed to commit people to the Solr server.", e);
         }
      });
   }

   @POST
   @Path("/reindex/works")
   public void reindexWorks()
   {
      withSolrClient(BibliographicSearchStrategy.SOLR_CORE, solr -> {
         // purge all existing records
         try
         {
            solr.deleteByQuery("*:*");
            solr.commit();
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Failed to remove data from the works core.", e);
         }

         BibliographicEntryRepository workRepository = repoRegistry.getRepository(null, BibliographicEntryRepository.class);

         Iterable<BibliographicEntry> works = () -> workRepository.listAll();

         Collection<SolrInputDocument> solrDocs = StreamSupport.stream(works.spliterator(), false)
               .flatMap(AdminResourceService::adapt)
               .collect(Collectors.toList());

         try
         {
            solr.add(solrDocs);
            solr.commit();
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Failed to commit works to the Solr server.", e);
         }
      });
   }

   @POST
   @Path("/reindex/relationships")
   public void reindexRelationships()
   {
      withSolrClient(RelnSearchStrategy.SOLR_CORE, relnSolrClient -> {
         try
         {
            // purge all existing records
            relnSolrClient.deleteByQuery("*:*");
            relnSolrClient.commit();
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Failed to remove data from the relationships core.", e);
         }

         RelationshipRepository relnRepository = repoRegistry.getRepository(null, RelationshipRepository.class);

         Iterable<Relationship> relationships = () -> relnRepository.listAll();
         Collection<SolrInputDocument> solrDocs = StreamSupport.stream(relationships.spliterator(), false)
               .map(RelnDocument::create)
               .collect(Collectors.toList());

         try
         {
            relnSolrClient.add(solrDocs);
            relnSolrClient.commit();
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Failed to commit relationships to the Solr server.", e);
         }
      });
   }

   private static Stream<SolrInputDocument> adapt(BibliographicEntry work)
   {
      try
      {
         SolrInputDocument workDoc = IndexAdapter.createWork(work);
         Stream<SolrInputDocument> editions = work.getEditions().stream()
               .flatMap(edition -> adapt(work.getId(), edition));
         return Stream.concat(Stream.of(workDoc), editions);
      }
      catch (Exception e)
      {
         String message = MessageFormat.format("Unable to adapt work {0}.", work.getId());
         logger.log(Level.WARNING, message, e);
         return Stream.empty();
      }
   }

   private static Stream<SolrInputDocument> adapt(String workId, Edition edition)
   {
      try
      {
         SolrInputDocument editionDoc = IndexAdapter.createEdition(workId, edition);
         Stream<SolrInputDocument> volumes = edition.getVolumes().stream()
               .map(volume -> adapt(workId, edition, volume))
               .filter(Objects::nonNull);
         return Stream.concat(Stream.of(editionDoc), volumes);
      }
      catch (Exception e)
      {
         String message = MessageFormat.format("Unable to adapt edition {0}/{1}.", workId, edition.getId());
         logger.log(Level.WARNING, message, e);
         return Stream.empty();
      }
   }

   private static SolrInputDocument adapt(String workId, Edition edition, Volume volume)
   {
      try
      {
         return IndexAdapter.createVolume(workId, edition, volume);
      }
      catch (Exception e)
      {
         String message = MessageFormat.format("Unable to adapt volume {0}/{1}/{2}.", workId, edition.getId(), volume.getId());
         logger.log(Level.WARNING, message, e);
         return null;
      }
   }

   private String extractFirstSentence(String text)
   {
      if (text == null || text.trim().isEmpty())
         return "";

      java.nio.file.Path sentenceModelPath = null;
      try {
         sentenceModelPath = config.getPropertyValue(BioSearchStrategy.OPENNLP_MODELS_SENTENCE_PATH, java.nio.file.Path.class);
      } catch (Exception ex) {
         // do nothing
      }

      if (sentenceModelPath != null)
      {
         try (InputStream modelInput = Files.newInputStream(sentenceModelPath))
         {
            SentenceModel sentenceModel = new SentenceModel(modelInput);
            SentenceDetectorME detector = new SentenceDetectorME(sentenceModel);
            String[] summarySentences = detector.sentDetect(text);
            return (summarySentences.length == 0) ? null : summarySentences[0];
         }
         catch (IOException e)
         {
            logger.log(Level.SEVERE, "Unable to open sentence detect model input file", e);
         }
      }

      int ix = text.indexOf(".");
      if (ix < 0)
         ix = text.length();

      return text.substring(0, Math.min(ix, 140));
   }
}
