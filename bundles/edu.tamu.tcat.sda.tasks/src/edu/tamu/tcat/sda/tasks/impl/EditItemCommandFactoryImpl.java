package edu.tamu.tcat.sda.tasks.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import edu.tamu.tcat.sda.tasks.EditWorkItemCommand;
import edu.tamu.tcat.sda.tasks.impl.PersistenceDtoV1.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.UpdateContext;
import edu.tamu.tcat.trc.resolver.EntryId;

public class EditItemCommandFactoryImpl implements EditCommandFactory<PersistenceDtoV1.WorkItem, EditWorkItemCommand>
{
   @Override
   public EditWorkItemCommand create(String id, EditCommandFactory.UpdateStrategy<WorkItem> strategy)
   {
      return new EditWorkItemCmdImpl(id, strategy);
   }

   @Override
   public EditWorkItemCommand edit(String id, EditCommandFactory.UpdateStrategy<WorkItem> strategy)
   {
      return new EditWorkItemCmdImpl(id, strategy);
   }


   public static class EditWorkItemCmdImpl implements EditWorkItemCommand
   {
      private final String id;
      private final EditCommandFactory.UpdateStrategy<WorkItem> strategy;
      private final ApplicableChangeSet<PersistenceDtoV1.WorkItem> changes = new BasicChangeSet<>();

      public EditWorkItemCmdImpl(String id, EditCommandFactory.UpdateStrategy<WorkItem> strategy)
      {
         this.id = id;
         this.strategy = strategy;
      }

      @Override
      public void setLabel(String label)
      {
         changes.add("", dto -> dto.label = label);
      }

      @Override
      public void setDescription(String description)
      {
         changes.add("", dto -> dto.description = description);
      }

      @Override
      public void setProperty(String key, String value)
      {
         changes.add("", dto -> dto.properties.put(key, value));
      }

      @Override
      public void clearProperty(String key)
      {
         changes.add("", dto -> dto.properties.remove(key));
      }

      @Override
      public void setEntityRef(String type, String id)
      {
         changes.add("", dto -> {
            EntryId eId = new EntryId(id, type);
            dto.entityRef = eId.toJsonForm();
         });
      }

      @Override
      public void setStage(WorkflowStage stage)
      {
         changes.add("", dto -> dto.stageId = stage.getId());
      }

      @Override
      public Future<String> execute()
      {
         CompletableFuture<PersistenceDtoV1.WorkItem> result = strategy.update(ctx -> {
            return changes.apply(prepareDto(ctx));
         });

         return result.thenApply(dto -> dto.id);
      }

      private PersistenceDtoV1.WorkItem prepareDto(UpdateContext<PersistenceDtoV1.WorkItem> ctx)
      {
         WorkItem original = ctx.getOriginal();
         PersistenceDtoV1.WorkItem dto = original != null
                     ? PersistenceDtoV1.WorkItem.copy(original)
                     : new PersistenceDtoV1.WorkItem();
         if (dto.id == null)
            dto.id = id;

         return dto;
      }
   }

}
