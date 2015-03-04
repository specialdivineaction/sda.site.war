package edu.tamu.tcat.catalogentries.relationship.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.catalogentries.relationship.model.RelationshipDV;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.ExecutionFailedException;

public class PsqlCreateRelationshipTask implements SqlExecutor.ExecutorTask<String>
{
   private final static String insert = "INSERT INTO relationships (id, relationship) VALUES(?,?)";

   private final RelationshipDV relationship;
   private final JsonMapper jsonMapper;

   public PsqlCreateRelationshipTask(RelationshipDV relationship, JsonMapper jsonMapper)
   {
      this.relationship = relationship;
      this.jsonMapper = jsonMapper;
   }

   private String getJson()
   {
      try
      {
         return jsonMapper.asString(relationship);
      }
      catch (JsonException jpe)
      {
         throw new IllegalArgumentException("Failed to serialize the supplied relationship [" + relationship + "]", jpe);
      }
   }

   @Override
   public String execute(Connection conn) throws Exception
   {
      try(PreparedStatement ps = conn.prepareStatement(insert))
      {
         PGobject jsonObject = new PGobject();
         jsonObject.setType("json");
         jsonObject.setValue(getJson());

         ps.setString(1, relationship.id);
         ps.setObject(2, jsonObject);

         int ct = ps.executeUpdate();
         if (ct != 1)
            throw new ExecutionFailedException("Failed to create work. Unexpected number of rows updates [" + ct + "]");

      }
      catch(SQLException e)
      {
         throw new IllegalStateException("Failed to create relationship: [" + relationship + "]");
      }
      return relationship.id;
   }

}
