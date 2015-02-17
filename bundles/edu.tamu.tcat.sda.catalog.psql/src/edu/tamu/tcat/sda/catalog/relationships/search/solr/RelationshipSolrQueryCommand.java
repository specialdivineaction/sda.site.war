package edu.tamu.tcat.sda.catalog.relationships.search.solr;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipDirection;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipQueryCommand;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipTypeRegistry;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

public class RelationshipSolrQueryCommand implements RelationshipQueryCommand
{
   private final static Logger logger = Logger.getLogger(RelationshipSolrQueryCommand.class.getName());
   private SolrQuery query;
   private StringBuilder sb;
   private RelationshipTypeRegistry typeReg;
   private SolrServer solr;

   public RelationshipSolrQueryCommand(SolrServer solr, RelationshipTypeRegistry typeReg)
   {
      this.solr = solr;
      this.typeReg = typeReg;
      query = new SolrQuery();
      sb = new StringBuilder();

      // HACK: Return all, until we build in a paging system.
      query.setRows(100);
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.catalog.relationships.search.solr.RelationshipQueryCommand#getResults()
    */
   @Override
   public Collection<Relationship> getResults()
   {
      Collection<Relationship> relationships = new HashSet<>();
      SolrDocumentList results = null;
      String relationshipJson = null;
      RelationshipDV dv = new RelationshipDV();
      QueryResponse response;
      try
      {
         response = solr.query(getQuery());
         results = response.getResults();
         for (SolrDocument result : results)
         {
            try
            {
               String relationship = result.getFieldValue("relationshipModel").toString();
               dv = SolrRelationshipSearchService.mapper.readValue(relationship, RelationshipDV.class);
               relationships.add(RelationshipDV.instantiate(dv, typeReg));
            }
            catch (IOException e)
            {
               logger.log(Level.SEVERE, "Failed to parse relationship record: [" + relationshipJson + "]. " + e);
            }
            catch (RelationshipException e)
            {
               logger.log(Level.SEVERE, "Error occurred while instantiating the relationship: [" + dv.id + "]. " + e);
            }
         }
      }
      catch (SolrServerException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }

      return relationships;
   }

   public SolrQuery getQuery()
   {
      query.setQuery(sb.toString().trim());
      return query;
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.catalog.relationships.search.solr.RelationshipQueryCommand#forEntity(java.net.URI, edu.tamu.tcat.sda.catalog.relationship.RelationshipDirection)
    */
   @Override
   public RelationshipQueryCommand forEntity(URI entity, RelationshipDirection direction)
   {

      switch(direction)
      {
         case any:
            sb.append(" (relatedEntities:" + entity.toString());
            sb.append(" OR ");
            sb.append("targetEntities:" + entity.toString() + ")");
            break;
         case to:
            sb.append(" targetEntities:" + entity.toString());
            break;
         case from:
            sb.append(" relatedEntities:" + entity.toString());
            break;
         default:
            throw new IllegalStateException("Relationship direction not defined");
      }
      return this;
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.catalog.relationships.search.solr.RelationshipQueryCommand#forEntity(java.net.URI)
    */
   @Override
   public RelationshipQueryCommand forEntity(URI entity)
   {
      return forEntity(entity, RelationshipDirection.any);
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.catalog.relationships.search.solr.RelationshipQueryCommand#byType(java.lang.String)
    */
   @Override
   public RelationshipQueryCommand byType(String typeId)
   {
      sb.append(" relationshipType:" + typeId);
      return this;
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.catalog.relationships.search.solr.RelationshipQueryCommand#setRowLimit(int)
    */
   @Override
   public RelationshipQueryCommand setRowLimit(int rows)
   {
      query.setRows(Integer.valueOf(rows));
      return this;
   }

   // TODO: decide on the proper way to organize the results
   /* (non-Javadoc)
    * @see edu.tamu.tcat.sda.catalog.relationships.search.solr.RelationshipQueryCommand#oderBy()
    */
   @Override
   public RelationshipQueryCommand oderBy()
   {
      return this;
   }

}
