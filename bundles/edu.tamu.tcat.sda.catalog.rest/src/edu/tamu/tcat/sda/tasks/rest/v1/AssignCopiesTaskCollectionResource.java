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
import edu.tamu.tcat.sda.tasks.dcopies.AssignCopiesEditorialTask;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;

public class AssignCopiesTaskCollectionResource
{
   private final Map<String, AssignCopiesEditorialTask> tasks;
   private WorkRepository workRepository;

   public AssignCopiesTaskCollectionResource(Map<String, AssignCopiesEditorialTask> tasks, WorkRepository workRepository)
   {
      this.tasks = tasks;
      this.workRepository = workRepository;
   }

   @GET
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
   public AssignCopiesWorklistResource getWorklistResource(@PathParam("id") String taskId)
   {
      AssignCopiesEditorialTask task = getTask(taskId);
      return new AssignCopiesWorklistResource(task, workRepository);
   }

   private AssignCopiesEditorialTask getTask(@PathParam("id") String taskId)
   {
      if (!tasks.containsKey(taskId))
      {
         throw new NotFoundException(MessageFormat.format("No task found for {0}", taskId));
      }

      return tasks.get(taskId);
   }
}
