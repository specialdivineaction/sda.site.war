package edu.tamu.tcat.sda.tasks.dcopies;

import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.trc.repo.EntityReference;

/**
 * Adapts persistence data transfer objects into domain models.
 */
public class ModelAdapter
{
   public static WorkItem adapt(PersistenceDtoV1.WorkItem item)
   {
      return new BasicWorkItem(
            item.id,
            item.label,
            item.description,
            item.properties,
            adapt(item.entityRef));
   }

   private static EntityReference adapt(PersistenceDtoV1.EntityId entityRef)
   {
      return new BasicEntityReference(entityRef.id, entityRef.type);
   }
}
