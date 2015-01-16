package edu.tamu.tcat.sda.catalog.relationships.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipTypeRegistry;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

public class PsqlGetRelationshipTask implements SqlExecutor.ExecutorTask<Relationship>
{
   private final static String select = "SELECT relationship FROM relationships"
                                      + "  WHERE id=? AND active=true";

   private final JsonMapper jsonMapper;
   private final String id;
   private final RelationshipTypeRegistry typeReg;

   public PsqlGetRelationshipTask(String id, JsonMapper jsonMapper, RelationshipTypeRegistry typeReg)
   {
      this.id = id;
      this.jsonMapper = jsonMapper;
      this.typeReg = typeReg;
   }

   @Override
   public Relationship execute(Connection conn) throws Exception
   {
      try (PreparedStatement ps = conn.prepareStatement(select))
      {
         ps.setString(1, id);
         try (ResultSet rs = ps.executeQuery())
         {
            if (!rs.next())
               throw new NoSuchCatalogRecordException("No catalog record exists for work id=" + id);

            PGobject pgo = (PGobject)rs.getObject("relationship");
            String relationshipJson = pgo.toString();
            try
            {
               RelationshipDV dv = jsonMapper.parse(relationshipJson, RelationshipDV.class);
               return RelationshipDV.instantiate(dv, typeReg);
            }
            catch (JsonException e)
            {
               throw new IllegalStateException("Failed to parse relationship record\n" + relationshipJson, e);
            }
         }
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Failed to retrieve relationship entry [entry id = " + id + "]", e);
      }
   }



}
