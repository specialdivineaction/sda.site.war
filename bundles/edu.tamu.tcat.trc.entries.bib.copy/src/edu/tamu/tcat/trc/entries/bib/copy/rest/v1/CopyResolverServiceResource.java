package edu.tamu.tcat.trc.entries.bib.copy.rest.v1;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverRegistryImpl;
import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverStrategy;
import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopy;
import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopyLinkRepository;
import edu.tamu.tcat.trc.entries.bib.copy.ResourceAccessException;

@Path("/copies")
public class CopyResolverServiceResource
{
   private static final Logger logger = Logger.getLogger(CopyResolverServiceResource.class.getName());
   CopyResolverRegistryImpl copyImpl = new CopyResolverRegistryImpl();
   private DigitalCopyLinkRepository dclRepo;

   public void setRepo(DigitalCopyLinkRepository dclRepo)
   {
      this.dclRepo = dclRepo;
   }

   public void activate()
   {
   }


   @GET
   @Path("{identifier}")
   @Produces(MediaType.APPLICATION_JSON)
   public DigitalCopy retrieve(@PathParam(value = "identifier") String id) throws ResourceAccessException, IllegalArgumentException
   {
      CopyResolverStrategy<?> strategy = copyImpl.getResolver(id);
      return strategy.resolve(id);
   }

   @POST
   @Path("{identifier}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public void createLink(@PathParam("identifier") String identifier, DigitalCopyLinkDTO copy)
   {
      CopyResolverStrategy<?> strategy = copyImpl.getResolver(identifier);

      if(strategy.canResolve(identifier))
      {
         dclRepo.create(copy);
      }
   }

}
