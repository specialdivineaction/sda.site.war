package edu.tamu.tcat.sda.catalog.rest.admin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Objects;
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
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.IndexAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.SolrDocAdapter;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.impl.search.RelnDocument;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

@Path("/admin")
public class AdminResourceService
{
   private static final Logger logger = Logger.getLogger(AdminResourceService.class.getName());

   // TODO we are creating a new SolrClient here instead of using WorkIndexService because
   //      WorkIndexService does not allow for batch indexing and purging.

   public static final String OPENNLP_MODELS_SENTENCE_PATH = "opennlp.models.sentence.path";

   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";
   public static final String SOLR_CORE_PEOPLE = "catalogentries.authors.solr.core";
   public static final String SOLR_CORE_WORKS = "catalogentries.works.solr.core";
   public static final String SOLR_CORE_RELATIONSHIPS = "catalogentries.relationships.solr.core";

   private ConfigurationProperties config;
   private SolrClient workSolrClient;
   private SolrClient peopleSolrClient;
   private SolrClient relnSolrClient;
   private BibliographicEntryRepository workRepository;
   private BiographicalEntryRepository peopleRepository;
   private RelationshipRepository relnRepository;

   public void setRepoRegistry(EntryRepositoryRegistry repoReg)
   {
      workRepository = repoReg.getRepository(null, BibliographicEntryRepository.class);
      peopleRepository = repoReg.getRepository(null, BiographicalEntryRepository.class);
      relnRepository = repoReg.getRepository(null, RelationshipRepository.class);
   }

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      Objects.requireNonNull(config, "No Configuration supplied.");
      Objects.requireNonNull(workRepository, "No work repository supplied.");
      Objects.requireNonNull(peopleRepository, "No people repository supplied.");
      Objects.requireNonNull(relnRepository, "No relationships repository supplied.");

      // TODO remove dependency on solrj from MANIFEST.MF once HttpSolrClient is removed

      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);

      String workSolrCore = config.getPropertyValue(SOLR_CORE_WORKS, String.class);
      URI workCoreUri = solrBaseUri.resolve(workSolrCore);
      workSolrClient = new HttpSolrClient(workCoreUri.toString());

      String peopleSolrCore = config.getPropertyValue(SOLR_CORE_PEOPLE, String.class);
      URI peopleCoreUri = solrBaseUri.resolve(peopleSolrCore);
      peopleSolrClient = new HttpSolrClient(peopleCoreUri.toString());

      String relnSolrCore = config.getPropertyValue(SOLR_CORE_RELATIONSHIPS, String.class);
      URI relnCoreUri = solrBaseUri.resolve(relnSolrCore);
      relnSolrClient = new HttpSolrClient(relnCoreUri.toString());
   }

   public void deactivate()
   {
      if (workSolrClient != null) {
         try
         {
            workSolrClient.close();
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Encountered an error while closing work Solr client.", e);
         }
      }

      if (peopleSolrClient != null) {
         try
         {
            peopleSolrClient.close();
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Encountered an error while closing people Solr client.", e);
         }
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
      try
      {
         peopleSolrClient.deleteByQuery("*:*");
         peopleSolrClient.commit();
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to remove data from the people core.", e);
      }

      Iterable<BiographicalEntry> people = () -> peopleRepository.listAll();

      SolrDocAdapter adapter = new SolrDocAdapter(this::extractFirstSentence);

      Collection<SolrInputDocument> solrDocs = StreamSupport.stream(people.spliterator(), false)
            .map(adapter::apply)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

      try
      {
         peopleSolrClient.add(solrDocs);
         peopleSolrClient.commit();
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to commit people to the Solr server.", e);
      }
   }

   @POST
   @Path("/reindex/works")
   public void reindexWorks()
   {
      // purge all existing records
      try
      {
         workSolrClient.deleteByQuery("*:*");
         workSolrClient.commit();
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to remove data from the works core.", e);
      }

      Iterable<BibliographicEntry> works = () -> workRepository.listAll();

      Collection<SolrInputDocument> solrDocs = StreamSupport.stream(works.spliterator(), false)
            .flatMap(AdminResourceService::adapt)
            .collect(Collectors.toList());

      try
      {
         workSolrClient.add(solrDocs);
         workSolrClient.commit();
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to commit works to the Solr server.", e);
      }
   }

   @POST
   @Path("/reindex/relationships")
   public void reindexRelationships()
   {
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

      Iterable<Relationship> relationships = () -> relnRepository.getAllRelationships();
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
         sentenceModelPath = config.getPropertyValue(OPENNLP_MODELS_SENTENCE_PATH, java.nio.file.Path.class);
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
