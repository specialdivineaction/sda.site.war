package edu.tamu.tcat.sda.tasks;

import java.util.concurrent.Future;

import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;

public interface EditWorkItemCommand
{
   void setLabel(String label);

   void setDescription(String description);

   void setProperty(String key, String value);

   void clearProperty(String key);

   void setEntityRef(String type, String id);

   void setStage(WorkflowStage stage);

   Future<String> execute();
}
