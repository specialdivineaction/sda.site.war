package edu.tamu.tcat.sda.rest;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.PeopleResource;

/**
 *  Serves as the central configuration point for the REST API. Delegates calls to resources
 *  provided by the TRC library.
 */
@Path("/")
public class RestApiService
{
   private TrcApplication trcCtx;

   public void setTrcApplication(TrcApplication trcMgr)
   {
      this.trcCtx = trcMgr;
   }

   public void activate()
   {
      // no-op
   }

   public void deactivate()
   {
      // no-op
   }


//   @Path("/search")
//   public Object getSearch()
//   {
//      return null;
//   }
//
//   @Path("/login")
//   public Object getLoginService()
//   {
//      return null;
//   }
//
//   @Path("/account")
//   public Object getAccountService()
//   {
//      return null;
//   }

   @Path("/entries/biographical")
   public PeopleResource getBiographicalEntries()
   {
      return new PeopleResource(trcCtx);
   }
//
//   @Path("/entries/bibliographical")
//   public Object getBibliographicalEntries()
//   {
//      return null;
//   }
//
//   @Path("/entries/relationships")
//   public Object getRelationships()
//   {
//      return null;
//   }
//
//   @Path("/entries/articles")
//   public Object getArticles()
//   {
//      return null;
//   }
//
//   @Path("/services/notes")
//   public Object getNotesService()
//   {
//      return null;
//   }
//
//   @Path("/services/tags")
//   public Object getTagsService()
//   {
//      return null;
//   }
//
//   @Path("/services/seeAlso")
//   public Object getSeeAlsoService()
//   {
//      return null;
//   }
//
//   @Path("/services/tasks")
//   public Object getTaskService()
//   {
//      return new EditorialTasksCollectionResource(trcCtx);
//   }
//
//   @Path("/services/workflow")
//   public Object getWorkflowService()
//   {
//      return null;
//   }
//
//   @Path("/services/bibref")
//   public Object getBibRefService()
//   {
//      return null;
//   }
//
//   @Path("/services/categorizations")
//   public Object getCategorizations()
//   {
//      return null;
//   }

}
