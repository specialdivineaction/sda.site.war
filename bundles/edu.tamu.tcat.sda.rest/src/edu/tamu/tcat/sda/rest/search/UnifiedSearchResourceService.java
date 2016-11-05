package edu.tamu.tcat.sda.rest.search;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.sda.rest.search.v1.UnifiedSearchResource;
import edu.tamu.tcat.trc.TrcApplication;

@Path("/")
public class UnifiedSearchResourceService
{
   private static final Logger logger = Logger.getLogger(UnifiedSearchResourceService.class.getName());

   private TrcApplication trcCtx;

   public void setAppContext(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
   }

   public void activate()
   {
      try
      {
         logger.info(() -> "Activating " + getClass().getSimpleName());

         Objects.requireNonNull(trcCtx, "No TRC application context provided");
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to activate " + getClass().getSimpleName(), e);
         throw e;
      }
   }

   // TODO might register a REST endpoint with the search service that can be
   //      returned. Could be stitched into the delegate

   @Path("search")
   public UnifiedSearchResource getDefaultVersion()
   {
      return getV1();
   }

   @Path("v1/search")
   public UnifiedSearchResource getV1()
   {
      return new UnifiedSearchResource(trcCtx);
   }
}
