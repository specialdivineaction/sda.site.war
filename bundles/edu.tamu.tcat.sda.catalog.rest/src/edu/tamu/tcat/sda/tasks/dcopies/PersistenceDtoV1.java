package edu.tamu.tcat.sda.tasks.dcopies;

import java.util.HashMap;
import java.util.Map;

public class PersistenceDtoV1
{
   public static class EntityId
   {
      public String type;
      public String id;
   }

   public static class WorkItem
   {
      public String id;
      public String label;
      public String description;
      public EntityId entityRef;

      public Map<String, String> properties = new HashMap<>();
      public String stageId;

      public static WorkItem copy(WorkItem orig)
      {
         WorkItem result = new WorkItem();
         result.id = orig.id;
         result.label = orig.label;
         result.description = orig.description;
         result.entityRef = new EntityId();
         result.entityRef.type = orig.entityRef.type;
         result.entityRef.id = orig.entityRef.id;
         result.stageId = orig.stageId;
         result.properties = new HashMap<>(orig.properties);

         return result;
      }
   }
}
