package edu.tamu.tcat.sda.catalog.rest;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
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
import edu.tamu.tcat.sda.catalog.works.EditWorkCommand;
import edu.tamu.tcat.sda.catalog.works.Edition;
import edu.tamu.tcat.sda.catalog.works.EditionMutator;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.CustomResultsDV;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;

@Path("/works/{workId}/editions")
public class EditionsResource
{
   private WorkRepository repo;


   // Called by DS
   public void setRepository(WorkRepository repo)
   {
      this.repo = repo;
   }

   // called by DS
   public void activate()
   {
   }

   // called by DS
   public void dispose()
   {
   }


   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Collection<EditionDV> listEditions(@PathParam(value = "workId") String workId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Work work = repo.getWork(workId);

      Collection<Edition> editions = work.getEditions();

      return editions.parallelStream()
            .map((e) -> new EditionDV(e))
            .collect(Collectors.toSet());
   }

   @GET
   @Path("{editionId}")
   @Produces(MediaType.APPLICATION_JSON)
   public EditionDV getEdition(@PathParam(value = "workId") String workId,
                                 @PathParam(value = "editionId") String editionId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Edition edition = repo.getEdition(workId, editionId);
      return new EditionDV(edition);
   }

   @PUT
   @Path("{editionId}")
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV updateEdition(@PathParam(value = "workId") String workId,
                               @PathParam(value = "editionId") String editionId, EditionDV edition) throws NoSuchCatalogRecordException, InterruptedException, ExecutionException
   {
      EditWorkCommand command = repo.edit(workId);
      EditionMutator editionMutator = command.editEdition(editionId);
      editionMutator.setAll(edition);
      command.execute();
      return new CustomResultsDV(editionMutator.getId());
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV createEdition(@PathParam(value = "workId") String workId, EditionDV edition) throws ExecutionException, NoSuchCatalogRecordException, InterruptedException
   {
      EditWorkCommand command = repo.edit(workId);
      EditionMutator editionMutator = command.createEdition();
      editionMutator.setAll(edition);
      command.execute();
      return new CustomResultsDV(editionMutator.getId());
   }
}