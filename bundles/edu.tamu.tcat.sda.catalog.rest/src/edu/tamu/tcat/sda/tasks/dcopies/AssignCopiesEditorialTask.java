package edu.tamu.tcat.sda.tasks.dcopies;

import java.util.function.Supplier;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.TaskSubmissionMonitor;
import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.workflow.BasicReviewedTaskWorkflow;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.repo.DocumentRepository;

/**
 * Hard-coded implementation of the 'AssignCopies' editorial task. To be re-factored once a
 * more flexible task definition process is in place.
 *
 */
public class AssignCopiesEditorialTask implements EditorialTask<Work>
{
   private static final Workflow workflow = new BasicReviewedTaskWorkflow();

   private final static String TABLE_NAME = "task_work_items";

   private final SqlExecutor sqlExecutor;

   public AssignCopiesEditorialTask(SqlExecutor sqlExecutor)
   {
      // TODO Auto-generated constructor stub

      this.sqlExecutor = sqlExecutor;
      // TODO create doc repo for use in serializing tasks
      buildDocumentRepository();
   }

   /**
    * @return A new document repository instance for persisting and retrieving works
    */
   private DocumentRepository<Work, EditWorkCommand> buildDocumentRepository()
   {
//      PsqlJacksonRepoBuilder<Work, EditWorkCommand, WorkDTO> repoBuilder = new PsqlJacksonRepoBuilder<>();
//
//      repoBuilder.setDbExecutor(sqlExecutor);
//      repoBuilder.setTableName(TABLE_NAME);
//      repoBuilder.setEditCommandFactory(new EditItemCommandFactoryImpl(idFactoryProvider, indexService));
//      repoBuilder.setDataAdapter(ModelAdapter::adapt);
//      repoBuilder.setSchema(buildSchema());
//      repoBuilder.setStorageType(WorkDTO.class);
//      repoBuilder.setEnableCreation(true);
//
//      try
//      {
//         return repoBuilder.build();
//      }
//      catch (RepositoryException e)
//      {
//         logger.log(Level.SEVERE, "Failed to construct work repository instance.", e);
//      }
      return null;
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
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();

   }

   @Override
   public void addItems(Supplier<Work> entities, TaskSubmissionMonitor monitor)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();

   }

}
