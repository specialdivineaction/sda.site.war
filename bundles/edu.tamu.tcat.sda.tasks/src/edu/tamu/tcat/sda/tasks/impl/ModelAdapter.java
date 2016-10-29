package edu.tamu.tcat.sda.tasks.impl;

import java.util.Objects;
import java.util.function.Function;

import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;

/**
 * Adapts persistence data transfer objects into domain models.
 */
public class ModelAdapter
{
   private final Function<String, WorkflowStage> stageResolver;

   public ModelAdapter(Function<String, WorkflowStage> stageResolver)
   {
      Objects.requireNonNull(stageResolver);
      this.stageResolver = stageResolver;
   }

   public WorkItem adapt(PersistenceDtoV1.WorkItem item)
   {

      return new BasicWorkItem(item, stageResolver);
   }
}
