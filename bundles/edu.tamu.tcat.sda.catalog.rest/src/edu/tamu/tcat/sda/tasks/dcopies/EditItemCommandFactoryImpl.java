package edu.tamu.tcat.sda.tasks.dcopies;

import java.util.function.Supplier;

import edu.tamu.tcat.sda.tasks.dcopies.PersistenceDtoV1.WorkItem;
import edu.tamu.tcat.trc.repo.CommitHook;
import edu.tamu.tcat.trc.repo.EditCommandFactory;

public class EditItemCommandFactoryImpl implements EditCommandFactory<PersistenceDtoV1.WorkItem, EditWorkItemCommand>
{

   @Override
   public EditWorkItemCommand create(String id, CommitHook<WorkItem> commitHook)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditWorkItemCommand edit(String id, Supplier<WorkItem> currentState, CommitHook<WorkItem> commitHook)
   {
      // TODO Auto-generated method stub
      return null;
   }


   public static class EditWorkItemCmdImpl implements EditWorkItemCommand
   {
      private final String itemId;
      private final Supplier<WorkItem> currentState;
      private final CommitHook<WorkItem> commitHook;

      // properties that are set by the command
      private String label;

      public EditWorkItemCmdImpl(String id, Supplier<WorkItem> currentState, CommitHook<WorkItem> commitHook)
      {
         itemId = id;
         this.currentState = currentState;
         this.commitHook = commitHook;
      }

      @Override
      public void setLabel()
      {
         // TODO Auto-generated method stub

      }

      @Override
      public void setDescription()
      {
         // TODO Auto-generated method stub

      }

      @Override
      public void setProperty(String key, String value)
      {
         // TODO Auto-generated method stub

      }

      @Override
      public void clearProperty(String key)
      {
         // TODO Auto-generated method stub

      }

   }

}
