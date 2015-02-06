package edu.tamu.tcat.sda.catalog.relationship.rest.v1;

import java.util.Collection;
import java.util.Objects;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.relationship.RelationshipException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipType;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipTypeRegistry;
import edu.tamu.tcat.sda.catalog.relationship.rest.v1.model.RelationshipTypeDTO;

@Path("/relationships/types")
public class RelationshipTypeService
{
   private static final Logger logger = Logger.getLogger(RelationshipTypeService.class.getName());

   private RelationshipTypeRegistry registry;

   public void setRegistry(RelationshipTypeRegistry registry)
   {
      this.registry = registry;
   }

   public void clearRegistry(RelationshipTypeRegistry registry)
   {
      this.registry = null;
   }

   public void activate()
   {
      Objects.requireNonNull(registry, "No type registry provided");
   }

   public void dispose()
   {
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{typeId}")
   public RelationshipTypeDTO getType(@PathParam(value = "typeId") String id)
   {
      // HACK: handle threading issues
      if (registry == null)
         throw new ServiceUnavailableException("Relationship types are currently unavailable.");

      try
      {
         RelationshipType relnType = registry.resolve(id);
         return RelationshipTypeDTO.create(relnType);
      }
      catch (RelationshipException e)
      {
         throw new NotFoundException("The relationship type [" + id + "] is not defined.");
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Collection<RelationshipTypeDTO> listDefinedTypes()
   {
      throw new UnsupportedOperationException();
   }

}
