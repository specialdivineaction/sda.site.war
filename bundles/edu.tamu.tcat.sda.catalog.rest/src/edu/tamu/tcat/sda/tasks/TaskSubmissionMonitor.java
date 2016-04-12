package edu.tamu.tcat.sda.tasks;

public interface TaskSubmissionMonitor
{
   /**
    * Called when the creation of an item succeeds.
    *
    * @param item
    * @param workItemId
    */
   <X> void created(TaskSubmissionMonitor.WorkItemCreationRecord<X> record);

   /**
    * Called when the creation of a work item fails.
    * @param error
    */
   <X> void failed(TaskSubmissionMonitor.WorkItemCreationError<X> error);

   void finished();

   public interface WorkItemCreationRecord<E>
   {
      E getItem();

      String getWorkItemId();
   }

   public interface WorkItemCreationError<E>
   {
      E getItem();

      String getMessage();

      Exception getException();
   }
}