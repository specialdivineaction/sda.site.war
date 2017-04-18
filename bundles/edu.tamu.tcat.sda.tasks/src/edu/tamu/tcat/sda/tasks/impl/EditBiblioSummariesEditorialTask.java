package edu.tamu.tcat.sda.tasks.impl;

import java.util.concurrent.Executor;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.workflow.BasicReviewedTaskWorkflow;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.trc.repo.id.IdFactory;

public class EditBiblioSummariesEditorialTask extends BiblioEditorialTask
{
   private static final Workflow workflow = new BasicReviewedTaskWorkflow();

   private static final String TABLE_NAME = "task_biblio_summaries";

   public EditBiblioSummariesEditorialTask(String id, SqlExecutor sqlExecutor, IdFactory idFactory, Executor executor)
   {
      super(id, sqlExecutor, idFactory, executor);
   }

   @Override
   public String getName()
   {
      return "Work Summaries";
   }

   @Override
   public String getDescription()
   {
      return "Review all bibliographic entries in the collection and edit summaries as necessary.";
   }

   @Override
   public Workflow getWorkflow()
   {
      return workflow;
   }

   @Override
   protected String getTableName()
   {
      return TABLE_NAME;
   }

}
