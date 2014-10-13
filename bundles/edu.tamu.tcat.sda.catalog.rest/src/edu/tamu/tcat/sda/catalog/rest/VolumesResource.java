package edu.tamu.tcat.sda.catalog.rest;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.works.Edition;
import edu.tamu.tcat.sda.catalog.works.Volume;
import edu.tamu.tcat.sda.catalog.works.Work;
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
                                           @PathParam(value = "editionId") String editionId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Work work = repo.getWork(Integer.parseInt(workId));
      Edition edition = work.getEdition(editionId);
      return edition.getVolumes().stream()
            .map(v -> new VolumeDV(v))
            .collect(Collectors.toSet());
   }

   @GET
   @Path("{volumeId}")
   @Produces(MediaType.APPLICATION_JSON)
   public VolumeDV getVolume(@PathParam(value = "workId") String workId,
                             @PathParam(value = "editionId") String editionId,
                             @PathParam(value = "volumeId") String volumeId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Work work = repo.getWork(Integer.parseInt(workId));
      Edition edition = work.getEdition(editionId);
      Volume volume = edition.getVolume(volumeId);
      return new VolumeDV(volume);
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
