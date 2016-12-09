package edu.tamu.tcat.sda.tasks.impl;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.trc.resolver.EntryId;

public class BasicWorkItem implements WorkItem
{
   private final String id;
   private final String label;
   private final String description;
   private final Map<String, String> properties;
   private final EntryId entryReference;
   private final WorkflowStage stage;

   public BasicWorkItem(DataModelV1.WorkItem item, Function<String, WorkflowStage> stageResolver)
   {
      this.id = item.id;
      this.label = item.label;
      this.description = item.description;
      this.properties = item.properties;
      this.entryReference = EntryId.fromMap(item.entityRef);
      this.stage = stageResolver.apply(item.stageId);
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
   public EntryId getEntryId()
   {
      return entryReference;
   }

   @Override
   public WorkflowStage getStage()
   {
      return stage;
   }
}
