package edu.tamu.tcat.sda.tasks.dcopies;

import edu.tamu.tcat.trc.repo.RecordEditCommand;

public interface EditWorkItemCommand extends RecordEditCommand
{
   void setLabel(String label);

   void setDescription(String description);

   void setProperty(String key, String value);

   void clearProperty(String key);

   void setEntityRef(String type, String id);
}
