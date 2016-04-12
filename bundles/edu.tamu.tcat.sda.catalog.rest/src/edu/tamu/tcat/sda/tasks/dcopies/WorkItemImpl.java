package edu.tamu.tcat.sda.tasks.dcopies;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;

public class WorkItemImpl implements WorkItem
{
   private WorkRepository repo;

   private String id;
   private String label;
   private String description;

   private Map<String, String> properties;

   public WorkItemImpl(WorkRepository repo, PersistenceDtoV1.WorkItem item)
   {
      this.repo = repo;

      this.id = item.id;
      this.label = item.label;
      this.description = item.description;

      this.properties = new HashMap<>(item.properties);
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

}
