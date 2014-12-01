package edu.tamu.tcat.sda.catalog.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.sda.catalog.CatalogRepoException;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.solr.WorksController;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.SimpleWorkDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;

@Path("/works")
public class WorksResource
{

//   private ConfigurationProperties properties;
   private WorkRepository repo;

   // called by DS
   public void setConfigurationProperties(ConfigurationProperties properties)
   {
//      this.properties = properties;
   }

   // called by DS
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

   public WorksResource()
   {
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<SimpleWorkDV> listWorks(@Context UriInfo ctx) throws CatalogRepoException, NoSuchCatalogRecordException
   {
      MultivaluedMap<String, String> queryParams = ctx.getQueryParameters();
      WorksController controller = new WorksController();
      // TODO need to add slicing/paging support
      // TODO add mappers for exceptions. CatalogRepoException should map to internal error.

      // HACK: This allows the title to be searched, filtering will be added.

      List<SimpleWorkDV> results = new ArrayList<SimpleWorkDV>();
      if(!queryParams.isEmpty())
         try
         {
            results = controller.query(queryParams);
         }
         catch (JsonException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      else
         return results;

      return Collections.unmodifiableList(results);
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public WorkDV createWork(WorkDV work)
   {
      CreateWorkObserver workObserver = new CreateWorkObserver(repo);
      repo.create(work, workObserver);
      try
      {
         Work result = workObserver.getResult();
         return new WorkDV(result);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Unable to create work", e);
      }
   }

   @PUT
   @Path("{workid}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public WorkDV updateWork(WorkDV work)
   {
      CreateWorkObserver workObserver = new CreateWorkObserver(repo);
      repo.update(work, workObserver);

      try
      {
         Work result = workObserver.getResult();
         return new WorkDV(result);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Unable to update work", e);
      }
   }

   @GET
   @Path("{workid}")
   @Produces(MediaType.APPLICATION_JSON)
   public WorkDV getWork(@PathParam(value = "workid") String id) throws NoSuchCatalogRecordException
   {
      Work w = repo.getWork(id);
      return new WorkDV(w);
   }

   @GET
   @Path("{workid}/authors/{authid}")
   @Produces(MediaType.TEXT_HTML)
   public String getAuthorsWorks(@PathParam(value = "workid") int workId,
		                         @PathParam(value = "authid") int authId)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<html><head><title>").append("Document: ").append(workId).append("</title></head>")
        .append("<h1> Work ").append(workId).append("</h1>")
        .append("<h1> Author ").append(authId).append("</h1>")
        .append("</html>");

      return sb.toString();
   }

   @GET
   @Path("{id}.json")
   @Produces(MediaType.APPLICATION_JSON)
   public Map<String, Integer> getWorkAsJson(@PathParam(value = "id") int id)
   {
      Map<String, Integer> result = new HashMap<>();
      result.put("id", Integer.valueOf(id));
      return result;
   }

   @PUT
   @Path("{id}")
   public String updateWork()
   {
      return null;
   }


   private static final class CreateWorkObserver extends DataUpdateObserverAdapter<String>
   {
      private final CountDownLatch latch;

      private final WorkRepository repo;

      private volatile Work result;
      private volatile ResourceCreationException exception = null;

      CreateWorkObserver(WorkRepository repo)
      {
         this.repo = repo;
         latch = new CountDownLatch(1);
      }

      @Override
      protected void onFinish(String workId)
      {
         // HACK: need to do string-based data identifiers
         try {
            this.result = repo.getWork(Integer.parseInt(workId));
         }
         catch (NumberFormatException | NoSuchCatalogRecordException e) {
            throw new IllegalStateException("Failed to retrieve work [" + workId + "]", e);
         }
         latch.countDown();
      }

      @Override
      protected void onError(String message, Exception ex)
      {
         // TODO this should be a 500 error - repo could not create the resource, likely SQL
         //      error. We should log. Possibly send message to admin.
         exception = new ResourceCreationException(message, ex);
         latch.countDown();
      }

      public Work getResult() throws Exception
      {
         // TODO need semantic exception

         try
         {
            // HACK: hard coded timeout
            latch.await(5, TimeUnit.SECONDS);
         }
         catch (InterruptedException ex)
         {
            // TODO DB time out. . . . may have succeeded, client should not retry.
            // FIXME need to be able to cancel execution!
            this.cancel();
         }

         if (exception != null)
            throw exception;

         if (result == null)
            throw new IllegalStateException("Failed to obtain created work.");

         return result;
      }
   }
}
