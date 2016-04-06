package edu.tamu.tcat.sda.tasks.dcopies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import edu.tamu.tcat.sda.tasks.rest.v1.RestApiV1;

public class WorkflowStagesProvider
{
   private static final RestApiV1.WorkflowStage createStage(String id, String label, String description)
   {
      RestApiV1.WorkflowStage stage = new RestApiV1.WorkflowStage();
      stage.id = id;
      stage.label = label;
      stage.description = description;
      stage.transitions = new ArrayList<>();

      return stage;
   }

   public WorkflowStagesProvider()
   {
      // setup core workflow stages
      RestApiV1.WorkflowStage pending = createStage("pending", "Not Started", "Not yet started.");
      RestApiV1.WorkflowStage pinned = createStage("pinned", "Pinned", "Task pinned for urgent attention.");
      RestApiV1.WorkflowStage inprogress = createStage("inprogress", "In Progress", "Work has begun on this task.");
      RestApiV1.WorkflowStage review = createStage("review", "Under Review", "Work has been completed for this task and should be reviewed.");
      RestApiV1.WorkflowStage complete = createStage("complete", "Completed", "All work has been completed and reviews for this task.");
      RestApiV1.WorkflowStage deferred = createStage("deferred", "Deferred", "Task deferred for later work.");

      // setup transitions -- pending
      addTransition(pending, inprogress, "Start");
      addTransition(pending, deferred, "Defer");
      addTransition(pending, pinned, "Pin");

      // setup transitions -- pinned
      addTransition(pinned, review, "Mark Completed");
      addTransition(pinned, inprogress, "Unpin");
      addTransition(pinned, deferred, "Defer");
      addTransition(pinned, complete, "Approved");

      // setup transitions -- inprogress
      addTransition(inprogress, review, "Mark Completed");
      addTransition(inprogress, pinned, "Pin");
      addTransition(inprogress, deferred, "Defer");
      addTransition(inprogress, complete, "Approved");

      // setup transitions -- review
      addTransition(review, complete, "Approved");
      addTransition(review, inprogress, "Reject");

      // setup transitions -- completed
      addTransition(complete, inprogress, "Reopen");

      // setup transitions -- deferred
      addTransition(deferred, inprogress, "Restart");
      addTransition(deferred, review, "Mark Completed");
      addTransition(deferred, complete, "Approved");
   }

   /**
    * Creates a workflow transition from the source stage to the destination stage with the
    * associated transition label.
    *
    * @param source
    * @param target
    * @param label
    */
   private void addTransition(RestApiV1.WorkflowStage source, RestApiV1.WorkflowStage target, String label)
   {
      RestApiV1.WorkflowStageTransition transition = new RestApiV1.WorkflowStageTransition();
      transition.sourceStage = source.id;
      transition.targetStage = target.id;
      transition.label = label;

      source.transitions.add(transition);
   }

   public Set<RestApiV1.WorkflowStage> getStages()
   {
      return Collections.emptySet();
   }
}
