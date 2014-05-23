package edu.tamu.tcat.sda.catalog.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.db.psql.DataSourceFactory;
import edu.tamu.tcat.oss.db.psql.PsqlDbExec;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.psql.PsqlHistoricalFigureRepo;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

@Path("/works")
public class WorksResource
{

   private JacksonJsonMapper mapper = new JacksonJsonMapper();
   private PsqlHistoricalFigureRepo repo = new PsqlHistoricalFigureRepo(getExecutor(), mapper);

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
   @Produces(MediaType.APPLICATION_JSON)
   public WorkDV createWork()
   {
      return null;
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
   
   private DbExecutor getExecutor()
   {
      String url = "jdbc:postgresql://localhost:5433/SDA";
      String user = "postgres";
      String pass = "";
      DataSourceFactory factory = new DataSourceFactory();
      return new PsqlDbExec(factory.getDataSource(url, user, pass));
   }
   
}
