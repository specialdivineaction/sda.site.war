package edu.tamu.tcat.sda.tasks.rest.v1;

import java.text.MessageFormat;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.PartialWorkItemSet;
import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;

/**
 *  Implements the REST API for the list of work items associated with a particular
 *  taks. This API is scoped to a particular user account so that the returned items reflect
 *  that account's view of the work to be performed within a task.
 */
public class WorklistResource
{
   private final EditorialTask<?> task;

   public WorklistResource(EditorialTask<?> task)
   {
      this.task = task;
   }

   @Path("{id}")
   public WorkItemResource getItem(@PathParam("id") String id)
   {
      WorkItem item = task.getItem(id).orElseThrow(() -> new NotFoundException(MessageFormat.format("Unable to find task with id {0}.", id)));
      return new WorkItemResource(task, item);
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorklistGroup listItems(@QueryParam("stage") String stageId,
                                            @QueryParam("start") @DefaultValue("0") int start,
                                            @QueryParam("max") @DefaultValue("10") int max)
   {
      Workflow workflow = task.getWorkflow();
      Map<String, WorkflowStage> stages = workflow.getStages().stream()
            .collect(Collectors.toMap(WorkflowStage::getId, o -> o));

      if (!stages.containsKey(stageId))
      {
         String message = MessageFormat.format("Unknown stage ID '{0}'.", stageId);
         throw new BadRequestException(message);
      }

      WorkflowStage stage = stages.get(stageId);
      PartialWorkItemSet itemSet = task.getItems(stage, start, max);
      return RepoAdapter.makeWorklistGroup(task, stage, itemSet);
   }
}
