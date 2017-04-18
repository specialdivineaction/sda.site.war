package edu.tamu.tcat.sda.tasks.impl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.EditWorkItemCommand;
import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.WorkItemRepository;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.repo.postgres.JaversProvider;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class WorkItemRepositoryImpl implements WorkItemRepository
{
   private static final Logger logger = Logger.getLogger(WorkItemRepositoryImpl.class.getName());

   private final DocumentRepository<WorkItem, EditWorkItemCommand> repo;
   private final IdFactory idFactory;

   public WorkItemRepositoryImpl(String tableName, SqlExecutor sqlExecutor, IdFactory idFactory, ModelAdapter modelAdapter, JaversProvider javersProvider)
   {
      this.idFactory = idFactory;
      this.repo = buildDocumentRepository(sqlExecutor, tableName, modelAdapter, javersProvider);
   }

   private static DocumentRepository<WorkItem, EditWorkItemCommand> buildDocumentRepository(SqlExecutor sqlExecutor, String tableName, ModelAdapter modelAdapter, JaversProvider javersProvider)
   {
      PsqlJacksonRepoBuilder<WorkItem, DataModelV1.WorkItem, EditWorkItemCommand> repoBuilder = new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setPersistenceId(tableName);
      repoBuilder.setEditCommandFactory(new EditItemCommandFactoryImpl());
      repoBuilder.setDataAdapter(modelAdapter::adapt);
      repoBuilder.setStorageType(DataModelV1.WorkItem.class);
      repoBuilder.setEnableCreation(true);
      repoBuilder.setJaversProvider(javersProvider);

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

   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.tasks.impl.WorkItemRepository#getAllItems()
    */
   @Override
   public Iterator<WorkItem> getAllItems()
   {
      return repo.listAll();
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.tasks.impl.WorkItemRepository#getItem(java.lang.String)
    */
   @Override
   public Optional<WorkItem> getItem(String id)
   {
      return repo.get(id);
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.tasks.impl.WorkItemRepository#createItem()
    */
   @Override
   public EditWorkItemCommand createItem()
   {
      String id = idFactory.get();
      return repo.create(null, id);
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.tasks.impl.WorkItemRepository#editItem(java.lang.String)
    */
   @Override
   public EditWorkItemCommand editItem(String id)
   {
      try
      {
         return repo.edit(null, id);
      }
      catch (RepositoryException e)
      {
         String message = MessageFormat.format("Unable to find item with ID {0}.", id);
         throw new IllegalArgumentException(message, e);
      }
   }

}
