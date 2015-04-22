package edu.tamu.tcat.trc.entries.bib.rest.v1;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.CopyRefDTO;
import edu.tamu.tcat.trc.entries.bib.CopyReference;
import edu.tamu.tcat.trc.entries.bib.CopyReferenceRepository;
import edu.tamu.tcat.trc.entries.bib.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.entries.bib.UpdateCanceledException;
import edu.tamu.tcat.trc.entries.bib.WorkRepository;

@Path("/copies")
public class CopiesReferenceResource
{
   private WorkRepository repo;
   private CopyReferenceRepository copiesRepo;

   // Called by DS
   public void setRepository(WorkRepository repo)
   {
      this.repo = repo;
   }

   public void setCopyRepository(CopyReferenceRepository repo)
   {
      copiesRepo = repo;
   }

   // called by DS
   public void activate()
   {
      Objects.requireNonNull(repo, "No bibliographic work repository configured");
      Objects.requireNonNull(copiesRepo, "No copy reference repository configured");
   }

   // called by DS
   public void dispose()
   {
      repo = null;
      copiesRepo = null;
   }

   @GET
   @Path("{entityId : works/.+}")
   @Produces(MediaType.APPLICATION_JSON)
   public List<CopyRefDTO> getByWorkId(@PathParam(value = "entityId") String entityId)
   {
      // FIXME requires error handling
      URI uri = URI.create(entityId);
      Set<CopyReference> matchedCopies = copiesRepo.getCopies(uri);
      return matchedCopies.parallelStream()
                          .map(CopyRefDTO::create)
                          .collect(Collectors.toList());
   }

   @GET
   @Path("{refId : [0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}}")
   @Produces(MediaType.APPLICATION_JSON)
   public CopyRefDTO getByRefId(@PathParam(value = "refId") String refId)
   {
      // TODO requires better error handling
      UUID id = UUID.fromString(refId);
      try
      {
         CopyReference reference = copiesRepo.get(id);
         return CopyRefDTO.create(reference);
      }
      catch (NoSuchCatalogRecordException e)
      {
         throw new NotFoundException("Could not find copy [" + refId +"]");
      }
   }

   /**
    * Updates an existing copy reference.
    *
    * @param refId
    * @return
    */
   @PUT
   @Path("{refId : [0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public String updateRef(@PathParam(value = "refId") String refId, CopyRefDTO dto)
   {
      // TODO extract IDs
      try
      {
         UUID id = UUID.fromString(refId);
         EditCopyReferenceCommand command = copiesRepo.edit(id);
      }
      catch (IllegalArgumentException | NoSuchCatalogRecordException arg)
      {
         throw new NotFoundException("Invalid reference id [" + refId + "]");
      }

      return refId;
   }

   /**
    * Add a new copy reference
    * @param entityId
    * @return
    * @throws UpdateCanceledException
    */
   @POST
   @Path("{entityId : works/.+}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public String createByWorkId(@PathParam(value = "entityId") String entityId, CopyRefDTO dto) throws UpdateCanceledException
   {
      URI entityUri = URI.create(entityId);
      if (!entityUri.equals(dto.associatedEntry))
         throw new IllegalArgumentException();     // TODO document why

      // TODO verify valid copy id

      EditCopyReferenceCommand command = copiesRepo.create();
      command.setAssociatedEntry(entityUri);
      command.setCopyId(dto.copyId);
      command.setTitle(dto.title);
      command.setSummary(dto.summary);
      command.setRights(dto.rights);

      // TODO requires better error handling
      Future<CopyReference> future = command.execute();
      return entityId;
   }


}
