package edu.tamu.tcat.sda.tasks.rest.v1;

import java.text.MessageFormat;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.WorkItem;

public class WorkItemResource
{

   private final EditorialTask<?> task;
   private final WorkItem item;

   public WorkItemResource(EditorialTask<?> task, WorkItem item)
   {
      this.task = task;
      this.item = item;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkItem getWorkItem()
   {
      return RepoAdapter.toDTO(item, task);
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkItem transitionItem(RestApiV1.ItemStageTransition transition)
   {
      String targetStageId = transition.stage;
      WorkItem updatedItem;
      try
      {
         updatedItem = task.transition(item, targetStageId);
      }
      catch (IllegalArgumentException e)
      {
         String message = MessageFormat.format("Unable to transition item {0} to stage {1}", item.getId(), targetStageId);
         throw new BadRequestException(message, e);
      }
      return RepoAdapter.toDTO(updatedItem, task);
   }

}
