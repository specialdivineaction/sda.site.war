package edu.tamu.tcat.sda.tasks.rest.v1;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;

public class TaskCollectionResource
{
   private final Map<String, EditorialTask<?>> tasks;

   public TaskCollectionResource(Map<String, EditorialTask<?>> tasks)
   {
      this.tasks = tasks;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RestApiV1.EditorialTask> getTasks()
   {
      return tasks.values().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());
   }

   @GET
   @Path("{id}/workflow")
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Workflow getWorkflowResource(@PathParam("id") String taskId)
   {
      EditorialTask<?> task = getTask(taskId);
      Workflow workflow = task.getWorkflow();
      return RepoAdapter.toDTO(workflow);
   }

   @Path("{id}/items")
   public WorklistResource getWorklistResource(@PathParam("id") String taskId)
   {
      EditorialTask<?> task = getTask(taskId);
      return new WorklistResource(task);
   }

   private EditorialTask<?> getTask(@PathParam("id") String taskId)
   {
      if (!tasks.containsKey(taskId))
      {
         throw new NotFoundException(MessageFormat.format("No task found for {0}", taskId));
      }

      return tasks.get(taskId);
   }
}
