package edu.tamu.tcat.sda.catalog.relationship.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.relationship.EditRelationshipCommand;
import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;
import edu.tamu.tcat.sda.catalog.relationship.rest.v1.model.RelationshipId;
import edu.tamu.tcat.sda.catalog.relationships.search.solr.SolrRelationshipSearchService;

@Path("/relationships")
public class RelationshipsCollectionService
{
   private static final Logger logger = Logger.getLogger(RelationshipsCollectionService.class.getName());

   private RelationshipRepository repo;
   private SolrRelationshipSearchService service;

   public void setRepository(RelationshipRepository repo)
   {
      this.repo = repo;
   }

   public void setRelationshipService(SolrRelationshipSearchService service)
   {
      this.service = service;

   }

   public void activate()
   {
   }

   public void dispose()
   {
   }

   // /relationships?entity=<uri>      return all entities related to the supplied entity
   // /relationships?entity=<uri>[&type=<type_id>][&direction=from|to|any]
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RelationshipDV> getRelationships()
   {
      // TODO - This will be taken care of on jira ticket https://issues.citd.tamu.edu/browse/RI-6
      List<RelationshipDV> relnDV = new ArrayList<>();
      Iterable<Relationship> foundRelns = service.findRelationshipsFor(URI.create("works/2"));
      for (Relationship reln : foundRelns)
      {
         relnDV.add(RelationshipDV.create(reln));
      }
      return relnDV;
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RelationshipId createRelationship(RelationshipDV relationship)
   {
      RelationshipId results = new RelationshipId();
      EditRelationshipCommand createCommand;
      try
      {
         createCommand = repo.create();
         createCommand.setAll(relationship);

         RelationshipId result = new RelationshipId();
         result.id = createCommand.execute().get();
      }
      catch (Exception e)
      {
         logger.severe("An error occured during the creating relationship process. Exception: " + e);
      }

      return results;
   }
}
