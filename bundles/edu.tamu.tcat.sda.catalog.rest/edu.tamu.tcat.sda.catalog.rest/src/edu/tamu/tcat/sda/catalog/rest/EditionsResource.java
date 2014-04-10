package edu.tamu.tcat.sda.catalog.rest;

import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/works/{workId}/editions")
public class EditionsResource
{

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   Collection<String> listEditions(@PathParam(value = "workId") String workId)
   {
      return Collections.emptySet();
   }
   
   @GET
   @Path("editionId")
   @Produces(MediaType.APPLICATION_JSON)
   Collection<String> getEdition(@PathParam(value = "workId") String workId,
                                 @PathParam(value = "editionId") String editionId)
   {
      return Collections.emptySet();
   }
   
}
