package edu.tamu.tcat.sda.tasks.rest.v1;

import java.util.List;
import java.util.Map;

public class RestApiV1
{

   /**
    * Defines the editorial status of key elements within the task and workflow definition
    * framework. This is designed to allow resources to be labeled as being edited pending
    * availability for use, active for use within the system and removed from availability
    * (inactive) for new uses.
    */
   public enum RevisionStatus
   {
      /** Resource has been created and is active within the system for new use. */
      Active,

      /** Resource is in the process of being created, but is not yet active in the system for use. */
      Pending,

      /** Resource definition has been created but is no longer available for use within the system. */
      Inactive;
   }

   /**
    *  Defines a general editorial task such as adding an article or updating digital copies.
    *  Tasks are intended to be accomplished in a multi-stage workflow in which each stage
    *  may be delegated to account holders with different assigned roles (for example,
    *  assigning an author, writing the article, reviewing the article, copy-editing).
    *
    *  <p>Tasks are ongoing work-processes that are performed on specific {@link WorkItem}s.
    *
    */
   public static class EditorialTask
   {
      public String id;

      public String label;

      public String description;

      public int numItems;

      public int activeItems;

      // HACK all tasks default to active for now
      public RevisionStatus status = RevisionStatus.Active;
   }

   /**
    *  Represents a specific unit of work to be performed in an editorial task. For example,
    *  this might be an article that is being written, a biographical entry that needs a review
    *  or a bibliographic entry that needs a work to be associated with it.
    *
    *  <p>The {@link WorkItem} is intended as a thin wrapper around some underlying content in
    *  TRC that manages the state of that item within the context of a particular task.
    */
   public static class WorkItem
   {
      /** Unique identifier for this work item. */
      public String itemId;

      /** Application defined type for the resource that is being referenced by this type. */
      public String type;

      /** Unique identifier for the entity represented by this item. */
      public String entityId;

      /** A label to use when displaying this work. */
      public String label;

      /** A simple key/value property store that allows applications to associate specific
       *  properties with a specific work item in order to better facilitate UI design. For
       *  example, this might be used to break out creator, title and publication date of
       *  bibliographic entries.
       */
      public Map<String, String> properties;

      /** The ID of the current workflow stage for this item. */
      public String stage;

      /** The ID of the editorial task this item is associated with. */
      public String task;

      // TODO add assignee, due date, start date, work estimate, priority, notes...
   }

   /**
    *  Updates to be supplied to an existing work item. This is the DTO accepted via the
    *  PATCH method.
    */
   public static class WorkItemUpdate
   {
      /** A label to use when displaying this work. */
      public String label;

      /** A simple key/value property store that allows applications to associate specific
       *  properties. Properties that are defined in this field will be updated in the
       *  underlying object. Properties supplied with a <code>null</code> value will be removed
       *  from the updated work item. */
      public Map<String, String> properties;

      // TODO add assignee, due date, start date, work estimate, priority
   }

   public static class ItemStageTransition
   {
      public String stage;

      public String comments;
   }

   public static class GroupedWorklist
   {
      public EditorialTask task;

      /** The query used to generate this worklist. */
      public WorklistQuery query;

      /** A map from stage id to a list of tasks that are associated with that stage. */
      public Map<String, WorklistGroup> itemGroups;

      // TODO allow alternate grouping mechanisms
   }

   public static class WorklistGroup
   {
      public String groupId;

      /**
       * Group label for display in the UI
       */
      public String label;

      /**
       * Total number of items in the group
       */
      public int itemCount;

      /**
       * Starting index offset of this subsequence of items;
       */
      public int start;

      /**
       * The maximum number of items that will appear in items.
       */
      public int max;

      /**
       * A subsequence of all items in the group, limited by offset and max count query parameters.
       * The number of items must be less than or equal to max.
       */
      public List<WorkItem> items;

      // TODO add links, sort-by, etc
   }

   public static class WorklistQuery
   {

   }

   /**
    * Basic descriptive information about a Workflow. A Workflow defines the
    * {@link WorkflowStage}s that items within an editorial task can pass through as well
    * as information about what account roles have permission to act on items or transition
    * items at each stage.
    */
   public static class Workflow
   {
      /** Unique identifier for this workflow. */
      public String id;

      /** Label (title) for display this workflow. */
      public String label;

      /** Brief description of the purpose and intent for this workflow. */
      public String description;

      /**
       * Lookup table for stages within this workflow.
       */
      public Map<String, WorkflowStage> stages;

      /** Defines the current status of this workflow. Workflows are initially created in the
       *  pending state and can be freely edited until activated. Once activated, only
       *  descriptive properties of the workflow can be chagned (e.g. labels and descriptions,
       *  but not defined stages or stage transitions). Workflows can be deactivated which will
       *  prevent their use in new context but allow existing uses to continue to function.
       */
      // HACK all workflows default to active
      public RevisionStatus status = RevisionStatus.Active;
   }

   public static class WorkflowStage
   {
      public String id;

      public String label;

      public String description;

      /** Indicates a final stage of the workflow. Items that have been moved to this stage
       *  will no longer be reflected in the count of active items within a task. */
      public boolean isFinal;

      /** A list of defined transitions between stages in the workflow. This will be in the
       *  order in which available transitions will be displayed in user interfaces. */
      public List<WorkflowStageTransition> transitions;
   }

   /**
    * Represents a transition from one stage in the workflow to another.
    */
   public static class WorkflowStageTransition
   {
      /** id of the workflow stage that is the starting point of this transition. */
      public String sourceStage;

      /** id of the workflow stage that is the ending point of this transition. */
      public String targetStage;

      /** Display label for indicating prompting the user to initiate this transition. */
      public String label;

      // TODO add descriptions for transitions
      // TODO add role based access to activate transition
   }
}
