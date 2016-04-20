package edu.tamu.tcat.sda.tasks.rest.v1;

import java.text.MessageFormat;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.dcopies.AssignCopiesEditorialTask;

public class AssignCopiesWorkItemResource
{

   private final AssignCopiesEditorialTask task;
   private final WorkItem item;

   public AssignCopiesWorkItemResource(AssignCopiesEditorialTask task, WorkItem item)
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
      throw new UnsupportedOperationException();
   }

}
