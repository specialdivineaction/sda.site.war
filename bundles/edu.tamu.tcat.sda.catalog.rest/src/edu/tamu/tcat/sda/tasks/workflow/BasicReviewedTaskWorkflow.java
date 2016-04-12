package edu.tamu.tcat.sda.tasks.dcopies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStateTransition;

public class CopyAssignmentWorkflow implements Workflow
{

   private List<WorkflowStageImpl> stages;
   private Map<String, List<WorkflowStageTransitionImpl>> transitions = new HashMap<>();

   public CopyAssignmentWorkflow()
   {
      initialize();
   }

   @Override
   public String getId()
   {
      return "copies";
   }

   @Override
   public String getName()
   {
      return "Associate Digital Copies";
   }

   @Override
   public String getDescription()
   {
      return "Review all bibliographic entries in the collection and associate digital "
            + "copies with each entry.";
   }

   @Override
   public List<WorkflowStage> getStages()
   {
      return Collections.unmodifiableList(stages);
   }

   @Override
   public WorkflowStage getInitialStage()
   {
      return stages.get(0);
   }

   private void initialize()
   {
      // setup core workflow stages
      WorkflowStageImpl pending = new WorkflowStageImpl("pending", "Not Started", "Not yet started.");
      WorkflowStageImpl pinned = new WorkflowStageImpl("pinned", "Pinned", "Task pinned for urgent attention.");
      WorkflowStageImpl inprogress = new WorkflowStageImpl("inprogress", "In Progress", "Work has begun on this task.");
      WorkflowStageImpl review = new WorkflowStageImpl("review", "Under Review", "Work has been completed for this task and should be reviewed.");
      WorkflowStageImpl complete = new WorkflowStageImpl("complete", "Completed", "All work has been completed and reviews for this task.");
      WorkflowStageImpl deferred = new WorkflowStageImpl("deferred", "Deferred", "Task deferred for later work.");

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

      stages = Arrays.asList(pending, pinned, inprogress, review, complete, deferred);
   }

   private void addTransition(WorkflowStageImpl from, WorkflowStageImpl dest, String label)
   {
      WorkflowStageTransitionImpl transition = new WorkflowStageTransitionImpl(label, label, from, dest);
      transitions.computeIfAbsent(from.getId(), (key) -> new ArrayList<>()).add(transition);
   }

   private class WorkflowStageImpl implements WorkflowStage
   {

      private final String id;
      private final String label;
      private final String description;

      public WorkflowStageImpl(String id, String label, String description)
      {
         this.id = id;
         this.label = label;
         this.description = description;
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
      public String getType()
      {
         return "";
      }

      @Override
      public List<WorkflowStateTransition> getTransitions()
      {
         List<WorkflowStageTransitionImpl> list = transitions.get(id);
         return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
      }
   }

   private static class WorkflowStageTransitionImpl implements WorkflowStateTransition
   {
      private final String id;
      private final String label;
      private final String description;
      private final WorkflowStage source;
      private final WorkflowStage dest;

      public WorkflowStageTransitionImpl(String label, String description, WorkflowStage source, WorkflowStage dest)
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
}
