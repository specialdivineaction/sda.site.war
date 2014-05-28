package edu.tamu.tcat.sda.catalog.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.oss.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorListDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDefinitionDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;

@Path("/works")
public class WorksResource
{

   private ConfigurationProperties properties;
   private WorkRepository repo;

   // called by DS
   public void setConfigurationProperties(ConfigurationProperties properties)
   {
      this.properties = properties;
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
   public Collection<String> listWorks()
   {
      return Arrays.asList("Thing 1", "Thing 2", "Red Fish", "Blue Fish");
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public WorkDV createWork(WorkDV work)
   {
      CreateWorkObserver workObserver = new CreateWorkObserver();
      repo.create(work, workObserver);
      
      try
      {
         Work result = workObserver.getResult();
         WorkDV workDV = new WorkDV();
         
         workDV.id = result.getId();
         workDV.title = new TitleDefinitionDV(result.getTitle());
         workDV.series = result.getSeries();
         workDV.authors = new AuthorListDV(result.getAuthors());
         workDV.pubInfo = new PublicationInfoDV(result.getPublicationInfo());
         workDV.otherAuthors = new AuthorListDV(result.getOtherAuthors());
         workDV.summary = result.getSummary();
         
         return workDV;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   @GET
   @Path("{workid}")
   @Produces(MediaType.TEXT_HTML)
   public String getWork(@PathParam(value = "workid") int id)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<html><head><title>").append("Document: ").append(id).append("</title></head>")
        .append("<h1> Work ").append(id).append("</h1>")
        .append("</html>");
      
      return sb.toString();
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
      result.put("id", id);
      return result;
   }
   
   @PUT
   @Path("{id}")
   public String updateWork()
   {
      return null;
   }
   

   private static final class CreateWorkObserver extends DataUpdateObserverAdapter<Work>
   {
      private final CountDownLatch latch;

      private volatile Work result;
      private volatile ResourceCreationException exception = null;
      
      CreateWorkObserver()
      {
         latch = new CountDownLatch(1);
      }

      @Override
      protected void onFinish(Work result)
      {
         this.result = result;
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
            latch.await(10, TimeUnit.MINUTES);
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
            throw new IllegalStateException("Failed to obtain created person.");
         
         return result;
      }
   }
}
