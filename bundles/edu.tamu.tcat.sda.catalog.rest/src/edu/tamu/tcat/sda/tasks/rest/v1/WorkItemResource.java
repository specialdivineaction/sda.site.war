package edu.tamu.tcat.sda.tasks.rest.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class WorkItemResource
{

   /**
    *
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkItem getWorkItem()
   {
      throw new UnsupportedOperationException();
   }

   public void transitionItem(RestApiV1.ItemStageTransition transition)
   {
      throw new UnsupportedOperationException();
   }


}