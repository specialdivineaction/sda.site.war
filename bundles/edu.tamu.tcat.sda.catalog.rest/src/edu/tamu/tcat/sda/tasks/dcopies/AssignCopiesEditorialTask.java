package edu.tamu.tcat.sda.tasks.dcopies;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.TaskSubmissionMonitor;
import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.BasicReviewedTaskWorkflow;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

/**
 * Hard-coded implementation of the 'AssignCopies' editorial task. To be re-factored once a
 * more flexible task definition process is in place.
 *
 */
public class AssignCopiesEditorialTask implements EditorialTask<Work>
{
   private static final String[] TITLE_PREFERENCE_ORDER = {"short", "canonical", "bibliographic"};

   private static final Logger logger = Logger.getLogger(AssignCopiesEditorialTask.class.getName());
   private static final Workflow workflow = new BasicReviewedTaskWorkflow();

   private final static String TABLE_NAME = "task_work_items";
   private static final String SCHEMA_ID = "sdaTaskWorkItem";
   private static final String SCHEMA_DATA_FIELD = "item";

   private final SqlExecutor sqlExecutor;
   private final IdFactory idFactory;
   private final DocumentRepository<WorkItem, EditWorkItemCommand> documentRepository;

   private final Executor executor;

   public AssignCopiesEditorialTask(SqlExecutor sqlExecutor, IdFactory idFactory, Executor executor)
   {
      this.sqlExecutor = sqlExecutor;
      this.idFactory = idFactory;
      this.executor = executor;
      this.documentRepository = buildDocumentRepository();
   }

   /**
    * @return A new document repository instance for persisting and retrieving works
    */
   private DocumentRepository<WorkItem, EditWorkItemCommand> buildDocumentRepository()
   {
      PsqlJacksonRepoBuilder<WorkItem, EditWorkItemCommand, PersistenceDtoV1.WorkItem> repoBuilder = new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setTableName(TABLE_NAME);
      repoBuilder.setEditCommandFactory(new EditItemCommandFactoryImpl());
      repoBuilder.setDataAdapter(ModelAdapter::adapt);
      repoBuilder.setSchema(buildSchema());
      repoBuilder.setStorageType(PersistenceDtoV1.WorkItem.class);
      repoBuilder.setEnableCreation(true);

      try
      {
         return repoBuilder.build();
      }
      catch (RepositoryException e)
      {
         logger.log(Level.SEVERE, "Failed to construct editorial task worklist item repository instance.", e);
      }
      return null;
   }

   private RepositorySchema buildSchema()
   {
      SchemaBuilder schemaBuilder = new BasicSchemaBuilder();
      schemaBuilder.setId(SCHEMA_ID);
      schemaBuilder.setDataField(SCHEMA_DATA_FIELD);
      return schemaBuilder.build();
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
   public Workflow getWorkflow()
   {
      return workflow;
   }

   public WorkItem getItem()
   {
      throw new UnsupportedOperationException();
   }

   public WorkItem getItem(Work entity)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public WorkItem addItem(Work entity) throws IllegalArgumentException
   {
      String id = idFactory.get();
      EditWorkItemCommand command = documentRepository.create(id);

      command.setEntityRef("work", entity.getId());
      command.setLabel(getLabel(entity));

      try
      {
         // NOTE: createdId should be the same as the generated id above, but the result of the
         //       execution is used here just to ensure consistency and to block on
         //       Future<String>#get().
         String createdId = command.execute().get();
         return documentRepository.get(createdId);
      }
      catch (InterruptedException | ExecutionException e)
      {
         throw new IllegalStateException("Unable to create work item", e);
      }
      catch (RepositoryException e)
      {
         throw new IllegalStateException("Work item supposedly created, but unable to retrieve it", e);
      }


   }

   @Override
   public void addItems(Supplier<Work> entities, TaskSubmissionMonitor monitor)
   {
      executor.execute(() -> {
         Work entity = entities.get();
         while (entity != null)
         {
            String id = idFactory.get();
            EditWorkItemCommand command = documentRepository.create(id);

            command.setEntityRef("work", entity.getId());
            command.setLabel(getLabel(entity));

            try
            {
               // NOTE: createdId should be the same as the generated id above, but the result of the
               //       execution is used here just to ensure consistency and to block on
               //       Future<String>#get().
               String createdItemId = command.execute().get();
               WorkItem workItem = documentRepository.get(createdItemId);
               monitor.created(new BasicWorkItemCreationRecord<>(workItem, createdItemId));
            }
            catch (ExecutionException | InterruptedException e)
            {
               // TODO: Not sure what to pass in as the first argument: Should it be the WorkItem (that was not created) or the Work?
               monitor.failed(new BasicWorkItemCreationError<>(null, "unable to fetch created work item for the given entity", e));
            }
            catch (RepositoryException e)
            {
               // TODO: Not sure what to pass in as the first argument: Should it be the WorkItem (that was not created) or the Work?
               monitor.failed(new BasicWorkItemCreationError<>(null, "unable to fetch created work item", e));
            }

            // prime next loop cycle
            entity = entities.get();
         }
         monitor.finished();
      });
   }

   private String getLabel(Work entity)
   {
      StringBuilder sb = new StringBuilder();

      AuthorList authors = entity.getAuthors();
      if (authors != null && authors.size() > 0)
      {
         AuthorReference author = authors.get(0);
         if (author != null)
         {
            String authorLastName = author.getLastName();

            if (authorLastName != null && !authorLastName.trim().isEmpty())
            {
               sb.append("<span class=\"author\">").append(authorLastName).append("</span>, ");
            }
         }
      }

      String formattedTitle = null;

      TitleDefinition titleDefinition = entity.getTitle();
      if (titleDefinition != null)
      {
         // find the first available title in preferred type order
         for (String type : TITLE_PREFERENCE_ORDER)
         {
            Title title = titleDefinition.get(type);

            if (title != null)
            {
               formattedTitle = title.getFullTitle();

               if (formattedTitle != null && !formattedTitle.trim().isEmpty())
               {
                  break;
               }
            }
         }

         if (formattedTitle == null || formattedTitle.trim().isEmpty())
         {
            // TODO: This is a placeholder for works that have no (preferred) title. Should this be a default string or an empty string?
            formattedTitle = "[untitled]";
         }
      }

      sb.append("<span class=\"title\">").append(formattedTitle).append("</span>");

      return sb.toString();
   }

   private static class BasicWorkItemCreationError<X> implements TaskSubmissionMonitor.WorkItemCreationError<X>
   {
      private final X item;
      private final String message;
      private final Exception exception;

      public BasicWorkItemCreationError(X item, String message, Exception exception)
      {
         this.item = item;
         this.message = message;
         this.exception = exception;
      }

      @Override
      public X getItem()
      {
         return item;
      }

      @Override
      public String getMessage()
      {
         return message;
      }

      @Override
      public Exception getException()
      {
         return exception;
      }
   }

   private static class BasicWorkItemCreationRecord<X> implements TaskSubmissionMonitor.WorkItemCreationRecord<X>
   {
      private final X item;
      private final String id;

      public BasicWorkItemCreationRecord(X item, String id)
      {
         this.item = item;
         this.id = id;
      }

      @Override
      public X getItem()
      {
         return item;
      }

      @Override
      public String getWorkItemId()
      {
         return id;
      }

   }
}
