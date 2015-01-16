package edu.tamu.tcat.sda.catalog.relationships.psql;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.IdFactory;
import edu.tamu.tcat.sda.catalog.psql.impl.EditRelationshipCommandImpl;
import edu.tamu.tcat.sda.catalog.relationship.EditRelationshipCommand;
import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipNotAvailableException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipPersistenceException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipTypeRegistry;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

public class PsqlRelationshipRepo implements RelationshipRepository
{

   public PsqlRelationshipRepo()
   {
      // TODO Auto-generated constructor stub
   }

   private static final String ID_CONTEXT = "relationships";
   private SqlExecutor exec;
   private IdFactory idFactory;
   private JsonMapper jsonMapper;
   private RelationshipTypeRegistry typeReg;

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setJsonMapper(JsonMapper mapper)
   {
      this.jsonMapper = mapper;
   }

   public void setIdFactory(IdFactory factory)
   {
      this.idFactory = factory;
   }

   public void setTypeRegistry(RelationshipTypeRegistry typeReg)
   {
      this.typeReg = typeReg;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(jsonMapper);
      Objects.requireNonNull(idFactory);
   }

   public void dispose()
   {
      this.exec = null;
      this.jsonMapper = null;
      this.idFactory = null;
   }

   @Override
   public Relationship get(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      PsqlGetRelationshipTask task = new PsqlGetRelationshipTask(id, jsonMapper, typeReg);
      try
      {
         return exec.submit(task).get();
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof RelationshipNotAvailableException)
            throw (RelationshipNotAvailableException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve relationship entry [" + id +"]" , e);
      }
      catch (InterruptedException e) {
         throw new IllegalStateException("Failed to retrieve relationship entry [" + id +"]", e);
      }
   }

   @Override
   public EditRelationshipCommand create() throws RelationshipPersistenceException
   {
      RelationshipDV relationship = new RelationshipDV();
      relationship.id = idFactory.getNextId(ID_CONTEXT);

      EditRelationshipCommandImpl command = new EditRelationshipCommandImpl(relationship, idFactory);
      command.setCommitHook((r) -> {
         PsqlCreateRelationshipTask task = new PsqlCreateRelationshipTask(r, jsonMapper);
         Future<String> submitRelationship = exec.submit(task);
         return submitRelationship;
      });
      return command;
   }

   @Override
   public EditRelationshipCommand edit(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      EditRelationshipCommandImpl command = new EditRelationshipCommandImpl(RelationshipDV.create(get(id)) , idFactory);
      command.setCommitHook((r) -> {
         PsqlUpdateRelationshipTask task = new PsqlUpdateRelationshipTask(r, jsonMapper);
         Future<String> submitRelationship = exec.submit(task);
         return submitRelationship;
      });
      return command;
   }

   @Override
   public void delete(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      throw new UnsupportedOperationException("not impl");
   }

}
