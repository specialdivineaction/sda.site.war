package edu.tamu.tcat.sda.tasks.rest.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Implements the REST API for an editorial task.
 */
public class TaskResource
{
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.EditorialTask getDetails()
   {
      throw new UnsupportedOperationException();
   }

   @Path("worklist")
   public WorklistResource getWorklist()
   {
      throw new UnsupportedOperationException();
   }
}
