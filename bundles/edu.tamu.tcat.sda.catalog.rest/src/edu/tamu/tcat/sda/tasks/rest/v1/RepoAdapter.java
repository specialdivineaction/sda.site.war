package edu.tamu.tcat.sda.tasks.rest.v1;

import java.util.stream.Collectors;

import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.trc.repo.EntityReference;

public class RepoAdapter
{
   public static RestApiV1.Workflow toDTO(Workflow workflow)
   {
      RestApiV1.Workflow dto = new RestApiV1.Workflow();

      dto.id = workflow.getId();
      dto.label = workflow.getName();
      dto.description = workflow.getDescription();

      return dto;
   }

   public static RestApiV1.WorkItem toDTO(WorkItem workItem)
   {
      RestApiV1.WorkItem dto = new RestApiV1.WorkItem();

      dto.itemId = workItem.getId();
      dto.label = workItem.getLabel();

      EntityReference reference = workItem.getEntityReference();
      if (reference != null)
      {
         dto.entityId = reference.getId();
         dto.type = reference.getType();
      }

      dto.properties = workItem.getProperties().stream()
            .collect(Collectors.toMap(o -> o, workItem::getProperty));

      WorkflowStage stage = workItem.getStage();
      if (stage != null)
      {
         dto.stage = stage.getId();
      }

      // TODO dto.task

      return dto;
   }

   public static RestApiV1.EditorialTask toDTO(EditorialTask<?> task)
   {
      RestApiV1.EditorialTask dto = new RestApiV1.EditorialTask();

      dto.id = task.getId();
      dto.description = task.getDescription();
      dto.label = task.getName();

      return dto;
   }
}
