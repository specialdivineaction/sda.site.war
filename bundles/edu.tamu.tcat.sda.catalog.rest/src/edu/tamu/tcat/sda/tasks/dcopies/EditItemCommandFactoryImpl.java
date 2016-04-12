package edu.tamu.tcat.sda.tasks.dcopies;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import edu.tamu.tcat.trc.repo.CommitHook;
import edu.tamu.tcat.trc.repo.EditCommandFactory;

public class EditItemCommandFactoryImpl implements EditCommandFactory<PersistenceDtoV1.WorkItem, EditWorkItemCommand>
{
   @Override
   public EditWorkItemCommand create(String id, CommitHook<PersistenceDtoV1.WorkItem> commitHook)
   {
      return new EditWorkItemCmdImpl(id, null, commitHook);
   }

   @Override
   public EditWorkItemCommand edit(String id, Supplier<PersistenceDtoV1.WorkItem> currentState, CommitHook<PersistenceDtoV1.WorkItem> commitHook)
   {
      return new EditWorkItemCmdImpl(id, currentState, commitHook);
   }


   public static class EditWorkItemCmdImpl implements EditWorkItemCommand
   {
      private final CommitHook<PersistenceDtoV1.WorkItem> commitHook;

      // properties that are set by the command
      private final WorkItemChangeSet changeSet;

      public EditWorkItemCmdImpl(String id, Supplier<PersistenceDtoV1.WorkItem> currentState, CommitHook<PersistenceDtoV1.WorkItem> commitHook)
      {
         this.commitHook = commitHook;

         this.changeSet = new WorkItemChangeSet(id);

         if (currentState != null) {
            changeSet.original = currentState.get();
            if (changeSet.original != null) {
               changeSet.label = changeSet.original.label;
               changeSet.description = changeSet.original.description;
               changeSet.properties = new HashMap<>(changeSet.original.properties);
               changeSet.entityRef = changeSet.original.entityRef;
            }
         }
      }

      @Override
      public void setLabel(String label)
      {
         changeSet.label = label;
      }

      @Override
      public void setDescription(String description)
      {
         changeSet.description = description;
      }

      @Override
      public void setProperty(String key, String value)
      {
         changeSet.properties.put(key, value);
      }

      @Override
      public void clearProperty(String key)
      {
         changeSet.properties.remove(key);
      }

      @Override
      public void setEntityRef(String type, String id)
      {
         changeSet.entityRef.type = type;
         changeSet.entityRef.id = id;
      }

      @Override
      public Future<String> execute()
      {
         PersistenceDtoV1.WorkItem dto = new PersistenceDtoV1.WorkItem();

         dto.id = changeSet.id;
         dto.label = changeSet.label;
         dto.description = changeSet.description;
         dto.properties = changeSet.properties;
         dto.entityRef = changeSet.entityRef;

         return commitHook.submit(dto);
      }

   }

}
