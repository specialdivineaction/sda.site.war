package edu.tamu.tcat.sda.tasks.dcopies;

import java.util.HashMap;
import java.util.Map;

/**
 * Records the updates to an original version of a WorkItem.
 */
public class WorkItemChangeSet
{
   public final String id;
   public String label;
   public String description;
   public Map<String, String> properties = new HashMap<>();
   public PersistenceDtoV1.EntityId entityRef = new PersistenceDtoV1.EntityId();
   public String stageId;

   public PersistenceDtoV1.WorkItem original;

   public WorkItemChangeSet(String id)
   {
      this.id = id;
   }
}
