package edu.tamu.tcat.sda.tasks.rest.v1;

import java.util.stream.Collectors;

import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.PartialWorkItemSet;
import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStageTransition;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;

public class RepoAdapter
{
   public static RestApiV1.Workflow toDTO(Workflow workflow)
   {
      RestApiV1.Workflow dto = new RestApiV1.Workflow();

      dto.id = workflow.getId();
      dto.label = workflow.getName();
      dto.description = workflow.getDescription();
      dto.stages = workflow.getStages().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toMap(o -> o.id, o -> o));

      return dto;
   }

   public static RestApiV1.WorkflowStage toDTO(WorkflowStage stage)
   {
      RestApiV1.WorkflowStage dto = new RestApiV1.WorkflowStage();

      dto.id = stage.getId();
      dto.label = stage.getLabel();
      dto.description = stage.getDescription();
      dto.transitions = stage.getTransitions().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      return dto;
   }

   public static RestApiV1.WorkflowStageTransition toDTO(WorkflowStageTransition transition)
   {
      RestApiV1.WorkflowStageTransition dto = new RestApiV1.WorkflowStageTransition();

      dto.label = transition.getLabel();

      WorkflowStage source = transition.getSource();
      if (source != null)
      {
         dto.sourceStage = source.getId();
      }

      WorkflowStage target = transition.getTarget();
      if (target != null)
      {
         dto.targetStage = target.getId();
      }

      return dto;
   }

   public static RestApiV1.WorkItem toDTO(WorkItem workItem, EditorialTask<?> task)
   {
      RestApiV1.WorkItem dto = new RestApiV1.WorkItem();

      dto.itemId = workItem.getId();
      dto.label = workItem.getLabel();

      EntryReference reference = workItem.getEntryReference();
      if (reference != null)
      {
         dto.entityId = reference.id;
         dto.type = reference.type;
      }

      dto.properties = workItem.getProperties().stream()
            .collect(Collectors.toMap(o -> o, workItem::getProperty));

      WorkflowStage stage = workItem.getStage();
      if (stage != null)
      {
         dto.stage = stage.getId();
      }

      dto.task = task.getId();

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

   public static RestApiV1.WorklistGroup makeWorklistGroup(EditorialTask<?> task, WorkflowStage stage, PartialWorkItemSet itemSet)
   {
      RestApiV1.WorklistGroup dto = new RestApiV1.WorklistGroup();

      dto.groupId = stage.getId();
      dto.label = stage.getLabel();
      dto.itemCount = itemSet.getTotalMatched();
      dto.start = itemSet.getStart();
      dto.max = itemSet.getLimit();
      dto.items = itemSet.getItems().stream()
            .map(item -> RepoAdapter.toDTO(item, task))
            .collect(Collectors.toList());

      return dto;
   }
}
