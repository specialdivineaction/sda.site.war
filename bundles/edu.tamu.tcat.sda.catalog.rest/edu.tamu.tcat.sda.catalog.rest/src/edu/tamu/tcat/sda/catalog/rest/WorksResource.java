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

@Path("/works")
public class WorksResource
{

   public WorksResource()
   {
      // TODO Auto-generated constructor stub
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Collection<String> listWorks()
   {
      return Arrays.asList("Thing 1", "Thing 2", "Red Fish", "Blue Fish");
   }
   
   @GET
   @Path("{id}.html")
   @Produces(MediaType.TEXT_HTML)
   public String getWorkHtml(@PathParam(value = "id") int id)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<html><head><title>").append("Document: ").append(id).append("</title></head>")
        .append("<h1>").append(id).append("</h1>")
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
   
   @POST
   public String createWork()
   {
      return null;
   }
   
   @PUT
   @Path("{id}")
   public String updateWork()
   {
      return null;
   }
   
   

}
