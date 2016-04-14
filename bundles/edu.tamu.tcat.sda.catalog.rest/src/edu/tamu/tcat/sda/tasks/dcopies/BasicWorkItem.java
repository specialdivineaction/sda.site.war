package edu.tamu.tcat.sda.tasks.dcopies;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.trc.repo.EntityReference;

public class BasicWorkItem implements WorkItem
{
   private final String id;
   private final String label;
   private final String description;
   private final Map<String, String> properties;
   private final EntityReference entityReference;
   private final WorkflowStage stage;

   public BasicWorkItem(String id,
                        String label,
                        String description,
                        Map<String, String> properties,
                        EntityReference entityReference,
                        WorkflowStage stage)
   {
      this.id = id;
      this.label = label;
      this.description = description;
      this.properties = properties;
      this.entityReference = entityReference;
      this.stage = stage;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getLabel()
   {
      return label;
   }

   @Override
   public String getDescription()
   {
      return description;
   }

   @Override
   public Set<String> getProperties()
   {
      return properties.keySet();
   }

   @Override
   public String getProperty(String key)
   {
      String msg = "Undefined property key {0} for work item {1} [{2}]";
      if (!properties.containsKey(key))
         throw new IllegalArgumentException(MessageFormat.format(msg, key, label, id));

      return properties.get(key);
   }

   @Override
   public EntityReference getEntityReference()
   {
      return entityReference;
   }

   @Override
   public WorkflowStage getStage()
   {
      return stage;
   }
}
