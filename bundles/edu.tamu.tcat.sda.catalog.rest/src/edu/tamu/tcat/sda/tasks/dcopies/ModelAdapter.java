package edu.tamu.tcat.sda.tasks.dcopies;

import java.util.Objects;
import java.util.function.Function;

import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.trc.repo.EntityReference;

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
      return new BasicWorkItem(
            item.id,
            item.label,
            item.description,
            item.properties,
            adapt(item.entityRef),
            stageResolver.apply(item.stageId));
   }

   private static EntityReference adapt(PersistenceDtoV1.EntityId entityRef)
   {
      return new BasicEntityReference(entityRef.id, entityRef.type);
   }
}
