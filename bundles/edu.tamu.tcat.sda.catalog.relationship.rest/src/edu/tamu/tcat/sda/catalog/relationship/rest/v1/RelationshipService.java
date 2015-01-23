package edu.tamu.tcat.sda.catalog.relationship.rest.v1;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import edu.tamu.tcat.sda.catalog.relationship.rest.v1.model.RelationshipId;

@Path("/relationships/{id}")
public class RelationshipService
{
   private static final Logger logger = Logger.getLogger(RelationshipService.class.getName());

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

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RelationshipDV get(@PathParam(value = "id") String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      // TODO translate these into REST API exceptions
      return RelationshipDV.create(repo.get(id));
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RelationshipId remove(@PathParam(value = "id") String id, RelationshipDV relationship)
   {
      RelationshipId results = new RelationshipId();
      EditRelationshipCommand updateCommand;
      try
      {
         updateCommand = repo.edit(id);
         updateCommand.setAll(relationship);
         return new RelationshipId(updateCommand.execute().get());
      }
      catch (Exception e)
      {
         logger.severe("An error occured during the udpating process. Exception: " + e);
      }

      return results;
   }

   @DELETE
   public void deleteRelationship(@PathParam(value = "id") String id)
   {

   }

}
