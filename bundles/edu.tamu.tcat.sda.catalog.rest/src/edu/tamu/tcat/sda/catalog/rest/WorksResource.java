package edu.tamu.tcat.sda.catalog.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
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
import edu.tamu.tcat.sda.catalog.InvalidDataException;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.solr.WorksController;
import edu.tamu.tcat.sda.catalog.works.EditWorkCommand;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.CustomResultsDV;
import edu.tamu.tcat.sda.catalog.works.dv.SimpleWorkDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

@Path("/works")
public class WorksResource
{
   private static final Logger logger = Logger.getLogger(WorksResource.class.getName());
//   private ConfigurationProperties properties;
   private WorkRepository repo;

   public WorksResource()
   {
   }

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


   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<SimpleWorkDV> listWorks(@Context UriInfo ctx) throws JsonException
   {
      MultivaluedMap<String, String> queryParams = ctx.getQueryParameters();
      WorksController controller = new WorksController();
      // TODO need to add slicing/paging support
      // TODO add mappers for exceptions. CatalogRepoException should map to internal error.
      return Collections.unmodifiableList(controller.query(queryParams));
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV createWork(WorkDV work) throws InterruptedException, ExecutionException
   {
      EditWorkCommand workCommand = repo.create();
      workCommand.setAll(work);
      String id = workCommand.execute().get();
      return new CustomResultsDV(id);
   }

   @PUT
   @Path("{workid}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV updateWork(@PathParam(value = "workid") String workId, WorkDV work)
   {
      try
      {
         EditWorkCommand workCommand = repo.edit(workId);
         workCommand.setAll(work);
         String resultId = workCommand.execute().get();
         return new CustomResultsDV(resultId);
      }
      catch (NoSuchCatalogRecordException ex)
      {
         String msg = "Cannot update bibliographic entry [" + workId + "]. The identified entry does not exist.";
         logger.log(Level.WARNING, msg, ex);
         throw new NotFoundException(msg);
      }
      catch (InvalidDataException ex)
      {
         String msg = "Cannot update bibliographic entry [" + workId + "]. The supplied data is invalid: " + ex.getMessage();
         logger.log(Level.WARNING, msg, ex);
         throw new BadRequestException(msg);
      }

      catch (Exception ex)
      {
         String msg = "Cannot update bibliographic entry [" + workId + "]. An internal error occurred. Please see server log for full details.";
         logger.log(Level.SEVERE, msg, ex);
         throw new InternalServerErrorException(msg);

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
   @Path("{id}.json")
   @Produces(MediaType.APPLICATION_JSON)
   public Map<String, Integer> getWorkAsJson(@PathParam(value = "id") int id)
   {
      Map<String, Integer> result = new HashMap<>();
      result.put("id", Integer.valueOf(id));
      return result;
   }
}
