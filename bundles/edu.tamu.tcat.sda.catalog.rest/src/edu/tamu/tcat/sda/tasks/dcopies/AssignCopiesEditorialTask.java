package edu.tamu.tcat.sda.tasks.dcopies;

import java.text.MessageFormat;
import java.util.Set;
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
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
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
   private final DocumentRepository<WorkItem, EditWorkItemCommand> itemDocumentRepository;

   private final Executor executor;

   public AssignCopiesEditorialTask(SqlExecutor sqlExecutor, IdFactory idFactory, Executor executor)
   {
      this.sqlExecutor = sqlExecutor;
      this.idFactory = idFactory;
      this.executor = executor;
      this.itemDocumentRepository = buildDocumentRepository();
   }

   /**
    * @return A new document repository instance for persisting and retrieving works
    */
   private DocumentRepository<WorkItem, EditWorkItemCommand> buildDocumentRepository()
   {
      PsqlJacksonRepoBuilder<WorkItem, EditWorkItemCommand, PersistenceDtoV1.WorkItem> repoBuilder = new PsqlJacksonRepoBuilder<>();

      ModelAdapter modelAdapter = new ModelAdapter(workflow::getStage);

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setTableName(TABLE_NAME);
      repoBuilder.setEditCommandFactory(new EditItemCommandFactoryImpl());
      repoBuilder.setDataAdapter(modelAdapter::adapt);
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

   /**
    * Retrieves a work item by ID.
    *
    * @param id
    * @return
    */
   public WorkItem getItem(String id)
   {
      try
      {
         return itemDocumentRepository.get(id);
      }
      catch (RepositoryException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new IllegalStateException(e);
      }
   }

   public WorkItem getItem(Work entity)
   {
      // TODO: how do we query the documentRepository for something like this?
      throw new UnsupportedOperationException();
   }

   @Override
   public WorkItem addItem(Work entity) throws IllegalArgumentException
   {
      String id = idFactory.get();
      EditWorkItemCommand command = itemDocumentRepository.create(id);

      command.setEntityRef("work", entity.getId());
      command.setLabel(getLabel(entity));

      try
      {
         // NOTE: createdId should be the same as the generated id above, but the result of the
         //       execution is used here just to ensure consistency and to block on
         //       Future<String>#get().
         String createdId = command.execute().get();
         return itemDocumentRepository.get(createdId);
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
            EditWorkItemCommand command = itemDocumentRepository.create(id);

            command.setEntityRef("work", entity.getId());
            command.setLabel(getLabel(entity));

            try
            {
               // NOTE: createdId should be the same as the generated id above, but the result of the
               //       execution is used here just to ensure consistency and to block on
               //       Future<String>#get().
               String createdItemId = command.execute().get();
               WorkItem workItem = itemDocumentRepository.get(createdItemId);
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

   /**
    * Formats an HTML label for a work to be used as the work item label.
    *
    * If an author cannot be found, the placeholder "[Author Unavailable]" will be used instead.
    * If a title cannot be found, the placeholder "[Title Unavailable]" will be used instead.
    *
    * @param entity
    * @return
    */
   private static String getLabel(Work entity)
   {
      String authorLabel = getAuthorLabel(entity);
      String titleLabel = getTitleLabel(entity);

      return MessageFormat.format(
            "<span class=\"author\">{0}</span>, <span class=\"title\">{1}</span>",
            authorLabel == null ? "[Author Unavailable]" : authorLabel,
            titleLabel == null ? "[Title Unavailable]" : titleLabel
      );
   }

   /**
    * Finds the first available author with a last name and returns that author's last name.
    *
    * @param entity
    * @return The first non-empty author's last name or <code>null</code> if one cannot be found.
    */
   private static String getAuthorLabel(Work entity)
   {
      if (entity != null)
      {
         AuthorList authors = entity.getAuthors();
         if (authors != null && authors.size() > 0)
         {
            // find first available author's last name
            for (AuthorReference author : authors)
            {
               if (author == null)
               {
                  continue;
               }

               String authorLastName = author.getLastName();
               if (authorLastName == null || authorLastName.trim().isEmpty())
               {
                  continue;
               }

               return authorLastName.trim();
            }
         }
      }

      return null;
   }

   /**
    * Finds the first available title in preference order (see TITLE_PREFERENCE_ORDER constant).
    *
    * @param entity
    * @return The first non-empty full title or <code>null</code> if one cannot be found.
    */
   private static String getTitleLabel(Work entity)
   {
      if (entity != null)
      {
         TitleDefinition titleDefinition = entity.getTitle();
         if (titleDefinition != null)
         {
            // find the first available title in preferred type order
            for (String type : TITLE_PREFERENCE_ORDER)
            {
               Title title = titleDefinition.get(type);
               String fullTitle = extractFullTitle(title);
               if (fullTitle != null)
               {
                  return fullTitle;
               }
            }

            // no preferred titles available; just get any title
            Set<Title> titles = titleDefinition.get();
            if (titles != null)
            {
               for (Title title : titles)
               {
                  String fullTitle = extractFullTitle(title);
                  if (fullTitle != null)
                  {
                     return fullTitle;
                  }
               }
            }
         }
      }

      return null;
   }

   /**
    * Extracts and formats the full title of a given Title object.
    *
    * @param title
    * @return A string representing the full title or <code>null</code> if one cannot be extracted.
    */
   private static String extractFullTitle(Title title)
   {
      if (title == null)
      {
         return null;
      }

      String fullTitle = title.getFullTitle();
      if (fullTitle == null || fullTitle.trim().isEmpty())
      {
         return null;
      }

      return fullTitle.trim();
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
      public X getEntity()
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
      public X getEntity()
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
