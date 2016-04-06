package edu.tamu.tcat.sda.tasks.workflow;

import java.util.List;

public interface Workflow
{
   String getId();

   String getName();

   String getDescription();

   List<WorkflowStage> getStages();

   WorkflowStage getInitialStage();
}
