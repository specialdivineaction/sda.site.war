package edu.tamu.tcat.sda.catalog.rest;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
   
   public WorkResource createWork()
   {
      return null;
   }

}
