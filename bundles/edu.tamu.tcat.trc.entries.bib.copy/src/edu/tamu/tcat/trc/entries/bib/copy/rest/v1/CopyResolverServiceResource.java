package edu.tamu.tcat.trc.entries.bib.copy.rest.v1;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverRegistryImpl;
import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverStrategy;
import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopy;
import edu.tamu.tcat.trc.entries.bib.copy.ResourceAccessException;

@Path("/copies")
public class CopyResolverServiceResource
{
   private static final Logger logger = Logger.getLogger(CopyResolverServiceResource.class.getName());
   CopyResolverRegistryImpl copyImpl = new CopyResolverRegistryImpl();

   public void activate()
   {
   }


   @GET
   @Path("{identifier}")
   @Produces(MediaType.APPLICATION_JSON)
   public DigitalCopy retrieve(@PathParam(value = "identifier") String id)
   {

      CopyResolverStrategy<?> strategy = copyImpl.getResolver(id);
      try
      {
         return strategy.resolve(id);
      }
      catch (IllegalArgumentException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (ResourceAccessException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return null;

   }

}
