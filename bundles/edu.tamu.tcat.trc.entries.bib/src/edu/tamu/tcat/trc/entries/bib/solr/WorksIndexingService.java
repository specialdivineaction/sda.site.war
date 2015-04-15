package edu.tamu.tcat.trc.entries.bib.solr;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.bib.Edition;
import edu.tamu.tcat.trc.entries.bib.Volume;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.WorkIndexServiceManager;
import edu.tamu.tcat.trc.entries.bib.WorkRepository;
import edu.tamu.tcat.trc.entries.bib.WorksChangeEvent;

public class WorksIndexingService implements WorkIndexServiceManager
{
   private final static Logger logger = Logger.getLogger(WorksIndexingService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "catalogentries.works.solr.core";

   // configured here for use by other classes in this package - these classes are effectively
   // delegates of this service's responsibilities
   static final ObjectMapper mapper;

   private WorkRepository repo;
   private ConfigurationProperties config;
   private SolrServer solr;
   private AutoCloseable registration;

   static {
      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public WorksIndexingService()
   {
   }

   public void setWorksRepo(WorkRepository repo)
   {
      this.repo = repo;
   }

   public void setConfiguration(ConfigurationProperties cp)
   {
      this.config = cp;
   }

   public void activate()
   {
      logger.fine("Activating SolrRelationshipSearchService");
      Objects.requireNonNull(repo, "No work repository supplied.");
      registration = repo.addAfterUpdateListener(this::onUpdate);

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrServer(coreUri.toString());

   }

   public void deactivate()
   {
      logger.info("Deactivating WorksIndexingService");

      unregisterRepoListener();
      releaseSolrConnection();
   }

   private void unregisterRepoListener()
   {
      if (registration != null)
      {
         try
         {
            registration.close();
         }
         catch (Exception e)
         {
            logger.log(Level.WARNING, "Failed to unregister update listener on works repository.", e);
         }
         finally {
            registration = null;
         }
      }
   }

   private void releaseSolrConnection()
   {
      logger.fine("Releasing connection to Solr server");
      if (solr != null)
      {
         try
         {
            solr.shutdown();
         }
         catch (Exception e)
         {
            logger.log(Level.WARNING, "Failed to cleanly shut down connection to Solr server.", e);
         }
      }
   }


   private void onUpdate(WorksChangeEvent evt)
   {
      try
      {
         switch(evt.getChangeType())
         {
            case CREATED:
               onCreate(evt.getWorkEvt());
               break;
            case MODIFIED:
               onUpdate(evt.getWorkEvt());
               break;
            case DELETED:
               onDelete(evt.getWorkEvt());
               break;
            default:
               logger.log(Level.INFO, "Unexpected work change event [" + evt.getWorkId() +"]: " + evt.getChangeType());
         }

      }
      catch(Exception e)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change to work [" + evt.getWorkId() +"]: " + evt, e);
      }
   }

   private void onCreate(Work workEvt)
   {
      isIndexed(workEvt.getId());
      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      WorkSolrProxy workProxy = WorkSolrProxy.createWork(workEvt);
      solrDocs.add(workProxy.getDocument());

      for(Edition edition : workEvt.getEditions())
      {
         WorkSolrProxy editionProxy = WorkSolrProxy.createEdition(workEvt.getId(), edition);
         solrDocs.add(editionProxy.getDocument());

         for(Volume volume : edition.getVolumes())
         {
            WorkSolrProxy volumeProxy = WorkSolrProxy.createVolume(workEvt.getId(), edition, volume);
            solrDocs.add(volumeProxy.getDocument());
         }
      }

      try
      {
         solr.add(solrDocs);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit the work id: [" + workEvt.getId() + "] to the SOLR server. " + e);
      }

   }

   private void onUpdate(Work workEvt)
   {
      //HACK: Until Change notifications are implemented we will remove all works and corresponding editions / volumes.
      //      Once removed we will re-add all entities from the work.
      onCreate(workEvt);
   }

   private void onDelete(Work workEvt)
   {
      //HACK: Until Change notifications are implemented we will remove all works and corresponding editions / volumes.
      //      Once removed we will re-add all entities from the work.
      onCreate(workEvt);
   }

   private void isIndexed(String id)
   {
      SolrQuery query = new SolrQuery();
      query.setQuery("id:" + id);
      QueryResponse response;
      try
      {
         response = solr.query(query);
         SolrDocumentList results = response.getResults();
         if (!results.isEmpty())
            removeWorks(id);
      }
      catch (SolrServerException e)
      {
         logger.log(Level.SEVERE, "Failed to query the work id: [" + id + "] from the SOLR server. " + e);
      }

   }

   private void removeWorks(String id)
   {
      List<String> deleteIds = new ArrayList<>();
      deleteIds.add(id);

      SolrQuery query = new SolrQuery();
      query.setQuery("id:" + id + "\\:*");
      try
      {
         QueryResponse response = solr.query(query);
         SolrDocumentList results = response.getResults();
         for(SolrDocument doc : results)
         {
            deleteIds.add(doc.getFieldValue("id").toString());
         }
         solr.deleteById(deleteIds);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to delete the work id: [" + id + "] from the the SOLR server. " + e);
      }

   }

}
