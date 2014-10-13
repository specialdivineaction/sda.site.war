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
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;

@Path("/works/{workId}/editions/{editionId}/volumes")
public class VolumesResource
{
   private WorkRepository repo;

   public void activate()
   {
   }

   public void dispose()
   {
   }

   public void setRepository(WorkRepository repo)
   {
      this.repo = repo;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Collection<VolumeDV> listVolumes(@PathParam(value = "workId") String workId,
                                           @PathParam(value = "editionId") String editionId)
   {
      return Collections.emptySet();
   }

   @GET
   @Path("{volumeId}")
   @Produces(MediaType.APPLICATION_JSON)
   public VolumeDV getVolume(@PathParam(value = "workId") String workId,
                             @PathParam(value = "editionId") String editionId,
                             @PathParam(value = "volumeId") String volumeId)
   {
      return null;
   }

   @PUT
   @Path("{volumeId}")
   @Consumes(MediaType.APPLICATION_JSON)
   public String updateVolume(@PathParam(value = "workId") String workId,
                              @PathParam(value = "editionId") String editionId,
                              @PathParam(value = "volumeId") String volumeId)
   {
      return null;
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public String createVolume(@PathParam(value = "workId") String workId,
                              @PathParam(value = "editionId") String editionId)
   {
      return null;
   }
}
