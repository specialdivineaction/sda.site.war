package edu.tamu.tcat.sda.tasks.rest.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.trc.entries.types.biblio.Work;

/**
 * Implements the REST API for an editorial task.
 */
public class TaskResource
{
   private final EditorialTask<Work> task;

   public TaskResource(EditorialTask<Work> task)
   {
      this.task = task;
   }

   @GET
   @Path("workflow")
   @Produces(MediaType.APPLICATION_JSON)
   public WorkflowResource getWorkflowResource()
   {
      Workflow workflow = task.getWorkflow();
      return new WorkflowResource(workflow);
   }

   @Path("items")
   public WorklistResource getWorklistResource()
   {
      return new WorklistResource(task);
   }
}
