package edu.tamu.tcat.sda.tasks;

import java.util.function.Supplier;

import edu.tamu.tcat.sda.tasks.rest.v1.RestApiV1.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;

/**
 *  Defines a general editorial task such as adding an article or updating digital copies.
 *  Tasks are intended to be accomplished in a multi-stage workflow in which each stage
 *  may be delegated to account holders with different assigned roles (for example,
 *  assigning an author, writing the article, reviewing the article, copy-editing).
 *
 *  <p>Tasks are ongoing work-processes that are performed on specific {@link WorkItem}s.
 *
 *  <p>To simplify the API, individual {@code EditorialTask} instances are intended to be scoped
 *  to the authorized user who intantiated them. All operations will operate within the scope
 *  of the permissions and access controls of that individual user.
 *
 * @param <EntityType>
 */
public interface EditorialTask<EntityType>
{
   /**
    * @return The unique identifier for this task.
    */
   String getId();

   /**
    * @return The name of this task.
    */
   String getName();

   /**
    * @return A short description for this task.
    */
   String getDescription();

   /**
    * @return The workflow used to transition items through this task.
    */
   Workflow getWorkflow();

   void addItem(EntityType entity) throws IllegalArgumentException;

   /**
    * Adds work items for all entities provided by a supplier as new tasks.
    *
    * @param entitySupplier An entity supplier. May supply many entities. Should return
    *       <code>null</code> when no more entities are available.
    * @param monitor
    */
   void addItems(Supplier<EntityType> entitySupplier, TaskSubmissionMonitor monitor);

   // TODO entity reference

   public interface TaskSubmissionMonitor
   {
      /**
       * Called when the creation of an item succeeds.
       *
       * @param item
       * @param workItemId
       */
      <X> void created(WorkItemCreationRecord<X> record);

      /**
       * Called when the creation of a work item fails.
       * @param error
       */
      <X> void failed(WorkItemCreationError<X> error);

      void finished();
   }

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
