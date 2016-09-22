package edu.tamu.tcat.sda.tasks.impl;

import java.util.concurrent.Executor;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.workflow.BasicReviewedTaskWorkflow;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;

/**
 * Hard-coded implementation of the 'AssignCopies' editorial task. To be re-factored once a
 * more flexible task definition process is in place.
 *
 */
public class AssignRelationshipsEditorialTask extends BiblioEditorialTask
{
   private static final Workflow workflow = new BasicReviewedTaskWorkflow();

   private static final String TABLE_NAME = "task_relns";
   private static final String SCHEMA_ID = "sdaTaskRelationships";
   private static final String SCHEMA_DATA_FIELD = "item";

   public AssignRelationshipsEditorialTask(String id, SqlExecutor sqlExecutor, IdFactory idFactory, Executor executor)
   {
      super(id, sqlExecutor, idFactory, executor);
   }

   @Override
   protected String getTableName()
   {
      return TABLE_NAME;
   }

   @Override
   protected RepositorySchema getRepositorySchema()
   {
      SchemaBuilder schemaBuilder = new BasicSchemaBuilder();
      schemaBuilder.setId(SCHEMA_ID);
      schemaBuilder.setDataField(SCHEMA_DATA_FIELD);
      return schemaBuilder.build();
   }

   @Override
   public String getName()
   {
      return "Associate Relationships";
   }

   @Override
   public String getDescription()
   {
      return "Review all bibliographic entries in the collection and associate relationships with each entry.";
   }

   @Override
   public Workflow getWorkflow()
   {
      return workflow;
   }

}
