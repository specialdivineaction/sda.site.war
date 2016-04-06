package edu.tamu.tcat.sda.tasks.workflow;

import java.util.List;

public interface WorkflowStage
{

      /**
       * @return A unique identifier for this stage.
       */
      String getId();

      /**
       * @return The display label for this stage.
       */
      String getLabel();

      /**
       * @return A description of this stage.
       */
      String getDescription();

      /**
       * TODO need a concrete type
       * @return
       */
      String getType();

      /**
       *
       * @return A list of defined transitions from this stage to other stages within the
       *    workflow. This will be in the order in which available transitions will be
       *    displayed in user interfaces.
       */
      List<WorkflowStateTransition> getTransitions();
}
