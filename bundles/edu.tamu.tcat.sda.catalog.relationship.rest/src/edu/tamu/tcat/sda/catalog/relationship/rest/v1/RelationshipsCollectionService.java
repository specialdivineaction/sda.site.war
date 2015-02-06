package edu.tamu.tcat.sda.catalog.relationship.rest.v1;

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
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;
import edu.tamu.tcat.sda.catalog.relationship.rest.v1.model.RelationshipId;

@Path("/relationships")
public class RelationshipsCollectionService
{
   private static final Logger logger = Logger.getLogger(RelationshipsCollectionService.class.getName());

   private RelationshipRepository repo;

   public void setRepository(RelationshipRepository repo)
   {
      this.repo = repo;
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
      return new ArrayList<>();
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
