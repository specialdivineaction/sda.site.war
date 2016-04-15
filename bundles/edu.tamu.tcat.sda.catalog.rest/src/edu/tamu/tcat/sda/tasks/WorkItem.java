package edu.tamu.tcat.sda.tasks;

import java.util.Set;

import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.trc.repo.EntityReference;

/**
 * Represents a unit of work within an {@link EditorialTask}. For example, this might represent
 * a single bibliographic item to which digital copies should be assigned, or a book for which
 * a book review should be provided.
 */
public interface WorkItem
{
   String getId();

   String getLabel();

   String getDescription();

   Set<String> getProperties();

   String getProperty(String key);

   /**
    * @return The workflow stage of this item. Will not be <code>null</code>.
    */
   WorkflowStage getStage();

   EntityReference getEntityReference();

   // TODO add assignee, due date, reporter, etc.
   // TODO track transition history
}
