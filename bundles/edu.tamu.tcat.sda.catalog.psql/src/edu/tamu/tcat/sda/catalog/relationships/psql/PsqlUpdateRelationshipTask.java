package edu.tamu.tcat.sda.catalog.relationships.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.ExecutionFailedException;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

public class PsqlUpdateRelationshipTask implements SqlExecutor.ExecutorTask<String>
{
   private final static String insert = "UPDATE relationships"
                                      + "  SET relationship = ?,"
                                      + "      modified = now()"
                                      + "  WHERE id = ?";

   private final RelationshipDV relationship;
   private final JsonMapper jsonMapper;

   public PsqlUpdateRelationshipTask(RelationshipDV relationship, JsonMapper jsonMapper)
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

         ps.setObject(1, jsonObject);
         ps.setString(2, relationship.id);

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
