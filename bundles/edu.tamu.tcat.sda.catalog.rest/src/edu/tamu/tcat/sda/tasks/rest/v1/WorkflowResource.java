package edu.tamu.tcat.sda.tasks.rest.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.tasks.workflow.Workflow;

public class WorkflowResource
{
   private final Workflow workflow;

   public WorkflowResource(Workflow workflow)
   {
      this.workflow = workflow;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Workflow getWorkflow()
   {
      return RepoAdapter.toDTO(workflow);
   }
}
