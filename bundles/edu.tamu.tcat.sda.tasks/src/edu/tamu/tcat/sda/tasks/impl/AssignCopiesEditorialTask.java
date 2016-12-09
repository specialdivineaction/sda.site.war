package edu.tamu.tcat.sda.tasks.impl;

import java.util.concurrent.Executor;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.workflow.BasicReviewedTaskWorkflow;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.trc.repo.id.IdFactory;

/**
 * Hard-coded implementation of the 'AssignCopies' editorial task. To be re-factored once a
 * more flexible task definition process is in place.
 *
 */
public class AssignCopiesEditorialTask extends BiblioEditorialTask
{
   private static final Workflow workflow = new BasicReviewedTaskWorkflow();

   private static final String TABLE_NAME = "task_work_items";

   public AssignCopiesEditorialTask(String id, SqlExecutor sqlExecutor, IdFactory idFactory, Executor executor)
   {
      super(id, sqlExecutor, idFactory, executor);
   }

   @Override
   protected String getTableName()
   {
      return TABLE_NAME;
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

}
