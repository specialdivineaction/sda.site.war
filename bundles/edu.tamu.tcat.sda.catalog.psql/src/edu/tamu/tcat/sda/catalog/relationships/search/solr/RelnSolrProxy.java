package edu.tamu.tcat.sda.catalog.relationships.search.solr;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.sda.catalog.relationship.Anchor;
import edu.tamu.tcat.sda.catalog.relationship.AnchorSet;
import edu.tamu.tcat.sda.catalog.relationship.Provenance;
import edu.tamu.tcat.sda.catalog.relationship.Relationship;

/**
 *  A data structure for representing the searchable fields associated with a {@link Relationship}.
 */
public class RelnSolrProxy
{

   // NOTE this is internal to the Solr search service. Probably/possibly add helper methods to retrieve
   //      the reln (using the repo owned by the service) and other data structures as needed.


   private static final DateTimeFormatter iso8601Formatter = DateTimeFormatter.ISO_INSTANT;
   // Solr field names
   private final static String relnId = "id";
   private final static String description = "description";
   private final static String descriptMimeType = "descriptionMimeType";
   private final static String relationshipType = "realtionshipType";
   private final static String relatedEntities = "relatedEntities";
   private final static String targetEntities = "targetEntities";
   private final static String provCreators = "provCreator";
   private final static String provCreateDate = "provCreateDate";
   private final static String provModifiedDate = "provModifiedDate";

   private SolrInputDocument document;

   public static RelnSolrProxy create(Relationship reln)
   {
      RelnSolrProxy proxy = new RelnSolrProxy();

      proxy.addDocumentId(reln.getId());
      proxy.addDescription(reln.getDescription());
      proxy.addMimeType(reln.getDescriptionFormat());
      proxy.addRelnType(reln.getType().getTitle());
      proxy.addRelatedEntities(reln.getRelatedEntities());
      proxy.addTargetEntities(reln.getTargetEntities());
      proxy.addProvenance(reln.getProvenance());

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

   void addRelatedEntities(AnchorSet set)
   {
      for (Anchor anchor : set.getAnchors())
      {
         for (URI uri : anchor.getEntryIds())
         {
            document.addField(relatedEntities, uri.toString());
         }
      }
   }

   void addTargetEntities(AnchorSet set)
   {
      for (Anchor anchor : set.getAnchors())
      {
         for (URI uri : anchor.getEntryIds())
         {
            document.addField(targetEntities, uri.toString());
         }
      }
   }

   void addProvenance(Provenance prov)
   {
      for (URI uri : prov.getCreators())
      {
         document.addField(provCreators, uri.toString());
      }
      Instant created = prov.getDateCreated();
      Instant modified = prov.getDateModified();
      document.addField(provCreateDate, (created != null) ? iso8601Formatter.format(created) : null);
      document.addField(provModifiedDate, (modified != null) ? iso8601Formatter.format(modified) : null);
   }

}
