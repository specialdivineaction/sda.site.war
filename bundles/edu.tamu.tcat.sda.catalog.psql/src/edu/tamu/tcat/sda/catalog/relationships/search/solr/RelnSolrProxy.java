package edu.tamu.tcat.sda.catalog.relationships.search.solr;

import java.util.Set;

import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.model.AnchorDV;
import edu.tamu.tcat.sda.catalog.relationship.model.ProvenanceDV;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

/**
 *  A data structure for representing the searchable fields associated with a {@link Relationship}.
 */
public class RelnSolrProxy
{

   // NOTE this is internal to the Solr search service. Probably/possibly add helper methods to retrieve
   //      the reln (using the repo owned by the service) and other data structures as needed.

   // Solr field names
   private final static String relnId = "id";
   private final static String description = "description";
   private final static String descriptMimeType = "descriptionMimeType";
   private final static String relationshipType = "relationshipType";
   private final static String relatedEntities = "relatedEntities";
   private final static String targetEntities = "targetEntities";
   private final static String provCreators = "provCreator";
   private final static String provCreateDate = "provCreateDate";
   private final static String provModifiedDate = "provModifiedDate";
   private final static String relationshipModel = "relationshipModel";

   private SolrInputDocument document;

   public static RelnSolrProxy create(Relationship reln)
   {
      RelnSolrProxy proxy = new RelnSolrProxy();
      RelationshipDV relnDV = RelationshipDV.create(reln);

      proxy.addDocumentId(relnDV.id);
      proxy.addDescription(relnDV.description);
      proxy.addMimeType(relnDV.descriptionMimeType);
      proxy.addRelnType(relnDV.typeId);
      proxy.addRelatedEntities(relnDV.relatedEntities);
      proxy.addTargetEntities(relnDV.targetEntities);
      proxy.addProvenance(relnDV.provenance);

      try
      {
         proxy.addRelationshipModel(SolrRelationshipSearchService.mapper.writeValueAsString(relnDV));
      }
      catch (JsonProcessingException e)
      {
         throw new IllegalStateException("Failed to serialize relationship DV", e);
      }

      return proxy;
   }

   public RelnSolrProxy()
   {
      document = new SolrInputDocument();
   }

   public SolrInputDocument getDocument()
   {
      return document;
   }

   void addRelationshipModel(String jsonReln)
   {
      document.addField(relationshipModel, jsonReln);
   }

   void addDocumentId(String id)
   {
      document.addField(relnId, id);
   }

   void addDescription(String relnDescription)
   {
      document.addField(description, relnDescription);
   }

   void addMimeType(String mimeType)
   {
      document.addField(descriptMimeType, mimeType);
   }

   void addRelnType(String relnType)
   {
      document.addField(relationshipType, relnType);
   }

   void addRelatedEntities(Set<AnchorDV> anchors)
   {
      for (AnchorDV anchor : anchors)
      {

         for (String uri : anchor.entryUris)
         {
            document.addField(relatedEntities, uri);
         }
      }
   }

   void addTargetEntities(Set<AnchorDV> anchors)
   {
      for (AnchorDV anchor : anchors)
      {

         for (String uri : anchor.entryUris)
         {
            document.addField(targetEntities, uri);
         }
      }
   }

   void addProvenance(ProvenanceDV prov)
   {
      for (String uri : prov.creatorUris)
      {
         document.addField(provCreators, uri);
      }
      String dateCreated = prov.dateCreated;
      String dateModified = prov.dateModified;
      document.addField(provCreateDate, (dateCreated != null) ? dateCreated : null);
      document.addField(provModifiedDate, (dateModified != null) ? dateModified : null);
   }

}
