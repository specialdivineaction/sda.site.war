package edu.tamu.tcat.sda.catalog.relationships.search.solr;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipChangeEvent;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipSearchIndexManager;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipSearchService;

/**
 *  TODO include documentation about expected fields and format of the solr core.
 *
 */
public class SolrRelationshipSearchService implements RelationshipSearchIndexManager, RelationshipSearchService
{
   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "catalogentries.relationships.solr.core";

   private final static Logger logger = Logger.getLogger(SolrRelationshipSearchService.class.getName());

   private RelationshipRepository repo;
   private AutoCloseable registration;

   private SolrServer solr;
   private ConfigurationProperties config;

   public SolrRelationshipSearchService()
   {
   }

   public void setRelationshipRepo(RelationshipRepository repo)
   {
      this.repo = repo;
   }

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      logger.fine("Activating SolrRelationshipSearchService");
      registration = repo.addUpdateListener(this::onUpdate);

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrServer(coreUri.toString());
   }

   public void deactivate()
   {
      logger.info("Deactivating SolrRelationshipSearchService");

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
            logger.log(Level.WARNING, "Failed to unregister update listener on relationship repository.", e);
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

   private void onUpdate(RelationshipChangeEvent evt)
   {
      // NOTE: since this is an event listener, it serves as a fault barrier
      try
      {
         switch (evt.getChangeType())
         {
            case CREATED:
               onCreate(evt.getRelationship());
               break;
            case MODIFIED:
               onChange(evt.getRelationship());
               break;
            case DELETED:
               onDelete(evt.getRelationshipId());
               break;
            default:
               logger.log(Level.INFO, "Unexpected relationship change event [" + evt.getRelationshipId() +"]: " + evt.getChangeType());
         }
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change to relationship [" + evt.getRelationshipId() +"]: " + evt, ex);
      }
   }

   private void onCreate(Relationship reln)
   {
      // TODO implement me
   }

   private void onChange(Relationship reln)
   {
      // TODO implement me
   }

   private void onDelete(String id)
   {
      // TODO implement me
   }

   @Override
   public Iterable<Relationship> findRelationshipsFor(URI entry)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Iterable<Relationship> findRelationshipsBy(URI creator)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
