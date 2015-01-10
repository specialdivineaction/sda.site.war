package edu.tamu.tcat.sda.catalog.relationship.model;

import java.util.Set;

public class RelationshipDV
{
   public String id;
   public String typeId;
   public String description;
   public String descriptionMimeType;
   public ProvenanceDV provenance;
   public Set<AnchorDV> relatedEntities;
   public Set<AnchorDV> targetEntities;
}
