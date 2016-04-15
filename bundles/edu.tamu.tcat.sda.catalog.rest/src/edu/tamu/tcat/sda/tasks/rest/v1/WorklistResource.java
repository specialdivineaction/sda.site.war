package edu.tamu.tcat.sda.tasks.rest.v1;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.trc.entries.types.biblio.Work;

/**
 *  Implements the REST API for the list of work items associated with a particular
 *  taks. This API is scoped to a particular user account so that the returned items reflect
 *  that account's view of the work to be performed within a task.
 */
public class WorklistResource
{
   private final EditorialTask<Work> task;

   public WorklistResource(EditorialTask<Work> task)
   {
      this.task = task;
   }

   @Path("{id}")
   public WorkItemResource getItem(@PathParam("id") String id)
   {
      return new WorkItemResource();
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
      List<WorkItem> items = task.getItems(stage);

      List<RestApiV1.WorkItem> results = items.stream()
            .skip(start - 1)
            .limit(max)
            .map(RepoAdapter::toDTO).collect(Collectors.toList());

      RestApiV1.WorklistGroup dto = new RestApiV1.WorklistGroup();

      dto.groupId = stageId;
      dto.itemCount = items.size();
      dto.items = results;
      dto.label = stage.getLabel();
      dto.start = start;

      return dto;
   }

   @POST
   public void addWorkItem(RestApiV1.WorkItem item)
   {
      throw new UnsupportedOperationException();
   }


}
