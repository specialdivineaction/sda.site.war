package edu.tamu.tcat.sda.tasks.rest;

import javax.ws.rs.Path;

import edu.tamu.tcat.sda.tasks.rest.v1.TaskResource;

@Path("/")
public class TaskRestApiService
{

   @Path("/v1/tasks/{task}")
   public TaskResource getJsonTaskResource()
   {
      throw new UnsupportedOperationException();
   }
   
}
