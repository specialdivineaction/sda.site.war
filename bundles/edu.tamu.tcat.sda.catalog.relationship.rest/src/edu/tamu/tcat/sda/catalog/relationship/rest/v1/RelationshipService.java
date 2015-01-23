package edu.tamu.tcat.sda.catalog.relationship.rest.v1;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.relationship.EditRelationshipCommand;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipNotAvailableException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipPersistenceException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

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
   public void update(@PathParam(value = "id") String id, RelationshipDV relationship)
   {
      checkRelationshipValidity(relationship, id);;
      try
      {
         EditRelationshipCommand updateCommand = repo.edit(id);
         updateCommand.setAll(relationship);

         // NOTE: we call get here to ensure that
         updateCommand.execute().get();
      }
      catch (Exception e)
      {
         // TODO Might check underlying cause of the exception and ensure that this isn't
         //      the result of malformed data.
         logger.log(Level.SEVERE, "An error occured during the udpating process.", e);
         throw new WebApplicationException("Failed to update relationship [" + id + "]", e.getCause(), 500);
      }
   }

   private void checkRelationshipValidity(RelationshipDV reln, String id)
   {
      if (!reln.id.equals(id))
      {
         String msg = "The id of the supplied relationship data [" + reln.id + "] does not match the id component of the URI [" + id + "]";
         logger.info("Bad Request: " + msg);
         throw new WebApplicationException(msg, 400);
      }

      // TODO need to supply additional checks for constraints on validity.
   }

   @DELETE
   public void remove(@PathParam(value = "id") String id)
   {

   }

}
