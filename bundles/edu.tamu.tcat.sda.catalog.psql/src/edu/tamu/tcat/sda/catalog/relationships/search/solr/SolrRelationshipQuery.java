package edu.tamu.tcat.sda.catalog.relationships.search.solr;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.response.JSONResponseWriter;
import org.noggit.JSONUtil;
import org.noggit.JSONWriter;

import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipTypeRegistry;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

public class SolrRelationshipQuery
{

   private final static Logger logger = Logger.getLogger(SolrRelationshipQuery.class.getName());

   public SolrQuery query;
   private Collection<Relationship> relationships;
   private JsonMapper jsonMapper;
   private RelationshipTypeRegistry typeReg;

   public SolrRelationshipQuery()
   {

   }

   public static SolrRelationshipQuery query(URI entry)
   {
      SolrRelationshipQuery solrRelnQuery = new SolrRelationshipQuery();
      solrRelnQuery.buildQuery(entry);
      return solrRelnQuery;
   }

   public Collection<Relationship> getResults(QueryResponse response, JsonMapper jsonMapper, RelationshipTypeRegistry typeReg)
   {
      this.jsonMapper = jsonMapper;
      this.typeReg = typeReg;
      relationships = new HashSet<>();
      String relationshipJson = null;
      RelationshipDV dv = new RelationshipDV();

      SolrDocumentList results = response.getResults();
      for (SolrDocument result : results)
      {
         try
         {
            // Get full object from solr field
            String json = JSONUtil.toJSON(result);
            relationshipJson = removeVersionNum(json);
            dv = this.jsonMapper.parse(relationshipJson, RelationshipDV.class);
            relationships.add(RelationshipDV.instantiate(dv, this.typeReg));
            return relationships;
         }
         catch (JsonException e)
         {
            logger.log(Level.SEVERE, "Failed to parse relationship record :[" + relationshipJson + "]. " + e);
         }
         catch (RelationshipException e)
         {
            logger.log(Level.SEVERE, "Error occurred while instantiating the relationship :[" + dv.id + "]. " + e);
         }

      }

      return relationships;
   }

   void buildQuery(URI entry)
   {
      query = new SolrQuery();
      query.setQuery("*:*");
   }

   String removeVersionNum(String json)
   {
      // HACK: Removes "SolrDocument" and "_version_=... from the result
      StringBuilder sb = new StringBuilder();
      int version = json.lastIndexOf(",");
      sb.append(json.substring(0, version));
      sb.append("}");
      return sb.toString();
   }


}
