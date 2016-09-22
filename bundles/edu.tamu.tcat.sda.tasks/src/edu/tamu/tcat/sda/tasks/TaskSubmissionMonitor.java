package edu.tamu.tcat.sda.tasks;

public interface TaskSubmissionMonitor
{
   /**
    * Called when the creation of an item succeeds.
    *
    * @param item
    * @param workItemId
    */
   <EntityType> void created(TaskSubmissionMonitor.WorkItemCreationRecord<EntityType> record);

   /**
    * Called when the creation of a work item fails.
    * @param error
    */
   <EntityType> void failed(TaskSubmissionMonitor.WorkItemCreationError<EntityType> error);

   void finished();

   public interface WorkItemCreationRecord<EntityType>
   {
      EntityType getEntity();

      String getWorkItemId();
   }

   public interface WorkItemCreationError<EntityType>
   {
      EntityType getEntity();

      String getMessage();

      Exception getException();
   }
}