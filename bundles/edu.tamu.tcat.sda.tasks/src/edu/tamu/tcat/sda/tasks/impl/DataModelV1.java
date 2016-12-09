package edu.tamu.tcat.sda.tasks.impl;

import java.util.HashMap;
import java.util.Map;

public class DataModelV1
{
   public static class WorkItem
   {
      public String id;
      public String label;
      public String description;
      public Map<String, String> entityRef = new HashMap<>();

      public Map<String, String> properties = new HashMap<>();
      public String stageId;

      public static WorkItem copy(WorkItem orig)
      {
         WorkItem result = new WorkItem();
         result.id = orig.id;
         result.label = orig.label;
         result.description = orig.description;
         result.entityRef = new HashMap<>(orig.entityRef);
         result.stageId = orig.stageId;
         result.properties = new HashMap<>(orig.properties);

         return result;
      }
   }
}
