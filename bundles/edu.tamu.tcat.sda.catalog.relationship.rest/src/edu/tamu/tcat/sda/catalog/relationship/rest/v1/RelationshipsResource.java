package edu.tamu.tcat.sda.catalog.relationship.rest.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.relationship.EditRelationshipCommand;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipNotAvailableException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipPersistenceException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

@Path("/relationships")
public class RelationshipsResource
{

   private Logger logger = Logger.getLogger("edu.tamu.tcat.sda.catalog.rest.relationshipsresource");
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

   public RelationshipsResource()
   {
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RelationshipDV> getRelationships()
   {
      // TODO - This will be taken care of on jira ticket https://issues.citd.tamu.edu/browse/RI-6
      return new ArrayList<>();
   }

   @GET
   @Path("{relationshipId}")
   @Produces(MediaType.APPLICATION_JSON)
   public RelationshipDV getRelationship(@PathParam(value = "relationshipId")String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      return RelationshipDV.create(repo.get(id));
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV createRelationship(RelationshipDV relationship)
   {
      CustomResultsDV results = new CustomResultsDV();
      EditRelationshipCommand createCommand;
      try
      {
         createCommand = repo.create();
         createCommand.setAll(relationship);
         return new CustomResultsDV(createCommand.execute().get());
      }
      catch (Exception e)
      {
         logger.severe("An error occured during the creating relationship process. Exception: " + e);
      }

      return results;
   }

   @PUT
   @Path("{relationshipId}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV updateRelationship(@PathParam(value = "relationshipId")String id,
                                             RelationshipDV relationship)
   {
      CustomResultsDV results = new CustomResultsDV();
      EditRelationshipCommand updateCommand;
      try
      {
         updateCommand = repo.edit(id);
         updateCommand.setAll(relationship);
         return new CustomResultsDV(updateCommand.execute().get());
      }
      catch (Exception e)
      {
         logger.severe("An error occured during the udpating process. Exception: " + e);
      }

      return results;
   }

   @DELETE
   @Path("{relationshipId}")
   public void deleteRelationship(@PathParam(value = "relationshipId") String id)
   {

   }

   public static class CustomResultsDV
   {
      public String id;

      public CustomResultsDV()
      {

      }

      public CustomResultsDV(String id)
      {
         this.id = id;

      }
   }
}
