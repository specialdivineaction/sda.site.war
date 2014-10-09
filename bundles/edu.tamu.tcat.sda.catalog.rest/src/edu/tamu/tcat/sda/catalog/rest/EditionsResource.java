package edu.tamu.tcat.sda.catalog.rest;

import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;

@Path("/works/{workId}/editions")
public class EditionsResource
{
   WorkRepository repo;

   void setRepository(WorkRepository repo)
   {
      this.repo = repo;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   Collection<String> listEditions(@PathParam(value = "workId") String workId)
   {
      return Collections.emptySet();
   }

   @GET
   @Path("{editionId}")
   @Produces(MediaType.APPLICATION_JSON)
   Collection<String> getEdition(@PathParam(value = "workId") String workId,
                                 @PathParam(value = "editionId") String editionId)
   {
      return Collections.emptySet();
   }

   @PUT
   @Path("{editionId}")
   public String updateEdition(@PathParam(value = "workId") String workId,
                               @PathParam(value = "editionId") String editionId)
   {
	   return null;
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public String createEdition(EditionDV edition)
   {
	   return null;
   }
}
