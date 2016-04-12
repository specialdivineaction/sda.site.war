package edu.tamu.tcat.sda.tasks.workflow;

class BasicWorkflowStageTransition implements WorkflowStateTransition
{
   private final String id;
   private final String label;
   private final String description;
   private final WorkflowStage source;
   private final WorkflowStage dest;

   public BasicWorkflowStageTransition(String label, String description, WorkflowStage source, WorkflowStage dest)
   {
      this.label = label;
      this.description = description;
      this.source = source;
      this.dest = dest;
      this.id = source.getId() + ":" + label;
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
   public WorkflowStage getSource()
   {
      return source;
   }

   @Override
   public WorkflowStage getTarget()
   {
      return dest;
   }

}