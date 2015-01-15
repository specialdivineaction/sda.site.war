package edu.tamu.tcat.sda.catalog.relationships.psql;

import java.util.Objects;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.IdFactory;
import edu.tamu.tcat.sda.catalog.relationship.EditRelationshipCommand;
import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipNotAvailableException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipPersistenceException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;

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
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditRelationshipCommand create() throws RelationshipPersistenceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditRelationshipCommand edit(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void delete(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      // TODO Auto-generated method stub

   }

}
