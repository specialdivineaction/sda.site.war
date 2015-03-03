package edu.tamu.tcat.sda.catalog.psql.test.data;

import java.util.HashSet;
import java.util.Set;

public class Relationships
{
   public Relationships(){}

   public RelationshipDV createRelationshipDV()
   {
      RelationshipDV relnDV = new RelationshipDV();
      relnDV.id = "";
      relnDV.description = "New Description";
      relnDV.descriptionMimeType = "MimeType";
      relnDV.typeId = "uk.ac.ox.bodleian.sda.relationships.influence";
      relnDV.provenance = createProvenance();
      relnDV.relatedEntities = createRelatedEntities();
      relnDV.targetEntities = createTargetEntities();
      return relnDV;
   }

   private ProvenanceDV createProvenance()
   {
      ProvenanceDV provDV = new ProvenanceDV();
      provDV.creatorUris = new HashSet<>();
      provDV.creatorUris.add("/people/1");
      provDV.creatorUris.add("/people/2");
      provDV.dateCreated = "2011-12-03T10:15:30Z";
      provDV.dateModified = "2011-12-03T10:15:30Z";
      return provDV;
   }

   private Set<AnchorDV> createRelatedEntities()
   {
      Set<AnchorDV> anchorDVs = new HashSet<>();
      Set<String> anchorUris = new HashSet<>();
      anchorUris.add("/works/2");
      anchorUris.add("/works/3");
      anchorDVs.add(createAnchorDV(anchorUris));
      return anchorDVs;
   }

   private Set<AnchorDV> createTargetEntities()
   {
      Set<AnchorDV> anchorDVs = new HashSet<>();
      Set<String> anchorUris = new HashSet<>();
      anchorUris.add("works/1");
      anchorUris.add("works/3");
      anchorDVs.add(createAnchorDV(anchorUris));
      return anchorDVs;
   }

   private AnchorDV createAnchorDV(Set<String> uris)
   {
      AnchorDV anchor = new AnchorDV();
      anchor.entryUris = uris;
      return anchor;
   }
}
