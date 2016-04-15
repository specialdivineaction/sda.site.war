package edu.tamu.tcat.sda.tasks.rest.v1;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.trc.entries.types.biblio.Work;

public class TaskCollectionResource
{
   private final EditorialTask<Work> task;

   public TaskCollectionResource(EditorialTask<Work> task)
   {
      this.task = task;
   }

   @GET
   public List<RestApiV1.EditorialTask> getTasks()
   {
      RestApiV1.EditorialTask dto = RepoAdapter.toDTO(task);
      return Arrays.asList(dto);
   }

   @GET
   @Path("{id}")
   public TaskResource getTaskResource(@PathParam("id") String taskId)
   {
      if (!Objects.equals(taskId, task.getId()))
      {
         throw new NotFoundException();
      }

      return new TaskResource(task);
   }
}
