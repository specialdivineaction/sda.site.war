package edu.tamu.tcat.sda.tasks.impl;

import java.util.HashMap;
import java.util.Map;

import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;

/**
 * Records the updates to an original version of a WorkItem.
 */
public class WorkItemChangeSet
{
   public final String id;
   public String label;
   public String description;
   public Map<String, String> properties = new HashMap<>();
   public EntryReference entityRef = new EntryReference();
   public String stageId;

   public PersistenceDtoV1.WorkItem original;

   public WorkItemChangeSet(String id)
   {
      this.id = id;
   }
}
