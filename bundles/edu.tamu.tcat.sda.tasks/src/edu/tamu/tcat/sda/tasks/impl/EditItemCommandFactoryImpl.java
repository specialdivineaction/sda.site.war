package edu.tamu.tcat.sda.tasks.impl;

import java.util.Optional;
import java.util.concurrent.Future;

import edu.tamu.tcat.sda.tasks.EditWorkItemCommand;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.ExecutableUpdateContext;
import edu.tamu.tcat.trc.resolver.EntryId;

public class EditItemCommandFactoryImpl implements EditCommandFactory<DataModelV1.WorkItem, EditWorkItemCommand>
{
   @Override
   public DataModelV1.WorkItem initialize(String id, Optional<DataModelV1.WorkItem> original)
   {
      return original.map(DataModelV1.WorkItem::copy)
            .orElseGet(() -> {
               DataModelV1.WorkItem dto = new DataModelV1.WorkItem();
               dto.id = id;
               return dto;
            });
   }

   @Override
   public EditWorkItemCommand create(ExecutableUpdateContext<DataModelV1.WorkItem> ctx)
   {
      return new EditWorkItemCmdImpl(ctx);
   }

   public static class EditWorkItemCmdImpl implements EditWorkItemCommand
   {
      private final ExecutableUpdateContext<DataModelV1.WorkItem> ctx;
      private final ApplicableChangeSet<DataModelV1.WorkItem> changes = new BasicChangeSet<>();

      public EditWorkItemCmdImpl(ExecutableUpdateContext<DataModelV1.WorkItem> ctx)
      {
         this.ctx = ctx;
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
         return ctx.update(changes::apply).thenApply(dto -> dto.id);
      }
   }

}
