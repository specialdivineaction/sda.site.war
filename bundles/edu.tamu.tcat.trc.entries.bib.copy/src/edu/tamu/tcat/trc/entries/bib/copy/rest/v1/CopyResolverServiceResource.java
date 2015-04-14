package edu.tamu.tcat.trc.entries.bib.copy.rest.v1;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverRegistryImpl;
import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverStrategy;
import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopy;
import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopyLinkRepository;
import edu.tamu.tcat.trc.entries.bib.copy.ResourceAccessException;
import edu.tamu.tcat.trc.entries.bib.copy.UnsupportedCopyTypeException;

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


   /**
    * Retrieves information about a HathiFile record given an identifier.
    *
    * NOTE: path parameters do not work with the '#' character in the identifier,
    *       so we are falling back on a URL-encoded query parameter for the moment
    * @param id
    * @return
    * @throws ResourceAccessException
    * @throws IllegalArgumentException
    */
   @GET
//   @Path("{identifier}")
   @Produces(MediaType.APPLICATION_JSON)
   public DigitalCopy retrieve(@QueryParam(value = "identifier") String id) throws ResourceAccessException, IllegalArgumentException
   {
      CopyResolverStrategy<?> strategy;
      try
      {
         strategy = copyImpl.getResolver(id);
      }
      catch (UnsupportedCopyTypeException e)
      {
         throw new IllegalArgumentException("Could not retrieve the digital copy [" + id + "]. No copy resolver has been registered that recognizes this type of copy.", e);
      }

      return strategy.resolve(id);
   }

   @POST
   @Path("{identifier}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public void createLink(@PathParam("identifier") String identifier, DigitalCopyLinkDTO copy)
   {
      CopyResolverStrategy<?> strategy;
      try
      {
         strategy = copyImpl.getResolver(identifier);
         dclRepo.create(copy);
      }
      catch (UnsupportedCopyTypeException e)
      {
         throw new IllegalArgumentException("Could not retrieve the digital copy [" + identifier + "]. No copy resolver has been registered that recognizes this type of copy.", e);
      }


   }

}
