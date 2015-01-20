package edu.tamu.tcat.sda.catalog.relationships.search.solr;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipChangeEvent;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipSearchIndexManager;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipSearchService;

public class SolrRelationshipSearchService implements RelationshipSearchIndexManager, RelationshipSearchService
{
   private final static Logger logger = Logger.getLogger(SolrRelationshipSearchService.class.getName());

   private RelationshipRepository repo;
   private AutoCloseable registration;

   public SolrRelationshipSearchService()
   {
      // TODO Auto-generated constructor stub
   }

   public void setRelationshipRepo(RelationshipRepository repo)
   {
      this.repo = repo;
   }

   public void activate()
   {
      logger.fine("Activating SolrRelationshipSearchService");
      registration = repo.addUpdateListener(this::onUpdate);
   }

   public void deactivate()
   {
      logger.fine("Deactivating SolrRelationshipSearchService");
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
