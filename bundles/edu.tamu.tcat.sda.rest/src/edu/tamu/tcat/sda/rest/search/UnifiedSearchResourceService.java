package edu.tamu.tcat.sda.rest.search;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.sda.rest.search.v1.UnifiedSearchResource;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

@Path("/")
public class UnifiedSearchResourceService
{
   private static final Logger logger = Logger.getLogger(UnifiedSearchResourceService.class.getName());

   private ConfigurationProperties config;
   private SearchServiceManager searchServiceMgr;

   public void setConfig(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setSearchServiceManager(SearchServiceManager searchServiceMgr)
   {
      this.searchServiceMgr = searchServiceMgr;
   }

   public void activate()
   {
      try
      {
         logger.info(() -> "Activating " + getClass().getSimpleName());

         Objects.requireNonNull(config, "No configuration provided");
         Objects.requireNonNull(searchServiceMgr, "No search service manager provided");
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to activate " + getClass().getSimpleName(), e);
         throw e;
      }
   }

   @Path("search")
   public UnifiedSearchResource getDefaultVersion()
   {
      return new UnifiedSearchResource(searchServiceMgr, config);
   }

   @Path("v1/search")
   public UnifiedSearchResource getV1()
   {
      return new UnifiedSearchResource(searchServiceMgr, config);
   }
}
