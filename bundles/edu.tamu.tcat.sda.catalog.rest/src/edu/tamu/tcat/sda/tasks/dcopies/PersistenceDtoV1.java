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
   }
}
