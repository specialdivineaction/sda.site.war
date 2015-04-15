package edu.tamu.tcat.trc.entries.bib.solr;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
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
   public static final String SOLR_CORE = "catalogentries.relationships.solr.core";

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
      Objects.requireNonNull(repo, "No relationship repository supplied.");
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
               logger.log(Level.INFO, "Unexpected relationship change event [" + evt.getWorkId() +"]: " + evt.getChangeType());
         }

      }
      catch(Exception e)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change to relationship [" + evt.getWorkId() +"]: " + evt, e);
      }
   }

   private void onCreate(Work workEvt)
   {
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
      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      WorkSolrProxy workProxy, editionProxy, volumeProxy;
      if(isIndexed(workEvt.getId()))
         workProxy = WorkSolrProxy.updateWork(workEvt);
      else
         workProxy = WorkSolrProxy.createWork(workEvt);

      solrDocs.add(workProxy.getDocument());

      for(Edition edition : workEvt.getEditions())
      {
         if(isIndexed(edition.getId()))
            editionProxy = WorkSolrProxy.updateEdition(workEvt.getId(), edition);
         else
            editionProxy = WorkSolrProxy.createEdition(workEvt.getId(), edition);
         solrDocs.add(editionProxy.getDocument());

         for(Volume volume : edition.getVolumes())
         {
            if(isIndexed(volume.getId()))
               volumeProxy = WorkSolrProxy.updateVolume(workEvt.getId(), edition, volume);
            else
               volumeProxy = WorkSolrProxy.createVolume(workEvt.getId(), edition, volume);

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

   private void onDelete(Work workEvt)
   {
      //Query to find which part of the work has been deleted
//      try
//      {
//         solr.deleteById(workEvt.getId());
//         solr.commit();
//      }
//      catch (SolrServerException | IOException e)
//      {
//         logger.log(Level.SEVERE, "Failed to remove work id: [" + workEvt.getId() + "] from the SOLR server. " + e);
//      }
   }

   private boolean isIndexed(String id)
   {
      SolrQuery query = new SolrQuery();
      query.setQuery("id:" + id);
      QueryResponse response;
      try
      {
         response = solr.query(query);
         SolrDocumentList results = response.getResults();
         return !results.isEmpty();
      }
      catch (SolrServerException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return false;
   }

}
