package edu.tamu.tcat.sda.catalog.rest;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/people")
public class PeopleResource
{
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<String> listPeople()
   {
      return Arrays.asList("Neal", "Jesse", "Paul", "That Other Guy");
   }

   @GET
   @Path("{personId}")
   @Produces(MediaType.APPLICATION_JSON)
   public String getPerson(@PathParam(value="personId") int personId)
   {
	   return null;
   }
}
