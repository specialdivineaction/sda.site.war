package edu.tamu.tcat.catalogentries.relationship.postgres;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Function;

import edu.tamu.tcat.catalogentries.IdFactory;
import edu.tamu.tcat.catalogentries.relationship.Anchor;
import edu.tamu.tcat.catalogentries.relationship.AnchorSet;
import edu.tamu.tcat.catalogentries.relationship.EditRelationshipCommand;
import edu.tamu.tcat.catalogentries.relationship.RelationshipType;
import edu.tamu.tcat.catalogentries.relationship.model.AnchorDV;
import edu.tamu.tcat.catalogentries.relationship.model.ProvenanceDV;
import edu.tamu.tcat.catalogentries.relationship.model.RelationshipDV;


public class EditRelationshipCommandImpl implements EditRelationshipCommand
{
   private final RelationshipDV relationship;

   private Function<RelationshipDV, Future<String>> commitHook;

   public EditRelationshipCommandImpl(RelationshipDV relationship, IdFactory idFactory)
   {
      this.relationship = relationship;
   }

   public void setCommitHook(Function<RelationshipDV, Future<String>> hook)
   {
      commitHook = hook;
   }

   @Override
   public void setAll(RelationshipDV relationship)
   {
       setTypeId(relationship.typeId);
       setDescription(relationship.description);
       setDescriptionFormat(relationship.descriptionMimeType);
       setProvenance(relationship.provenance);
       for (AnchorDV anchor : relationship.relatedEntities)
       {
          addRelatedEntity(anchor);
       }
       for (AnchorDV anchor : relationship.targetEntities)
       {
          addTargetEntity(anchor);
       }
   }

   @Override
   public void setTypeId(String typeId)
   {
      relationship.typeId = typeId;
   }

   @Override
   public void setType(RelationshipType typeRelationship)
   {
      // TODO
   }

   @Override
   public void setDescription(String description)
   {
      relationship.description = description;
   }

   @Override
   public void setDescriptionFormat(String descriptionFormat)
   {
      relationship.descriptionMimeType = descriptionFormat;
   }

   @Override
   public void setProvenance(ProvenanceDV provenance)
   {
      relationship.provenance = provenance;
   }

   @Override
   public void setRelatedEntities(AnchorSet related)
   {
      if (related != null)
      {
         relationship.relatedEntities = new HashSet<>();
         for (Anchor anchor : related.getAnchors())
         {
            relationship.relatedEntities.add(AnchorDV.create(anchor));
         }
      }
   }

   @Override
   public void addRelatedEntity(AnchorDV anchor)
   {
      relationship.relatedEntities.add(anchor);
   }

   @Override
   public void removeRelatedEntity(AnchorDV anchor)
   {
      relationship.relatedEntities.remove(anchor);
   }

   @Override
   public void setTargetEntities(AnchorSet target)
   {
      if (target != null)
      {
         relationship.targetEntities = new HashSet<>();
         for (Anchor anchor : target.getAnchors())
         {
            relationship.targetEntities.add(AnchorDV.create(anchor));
         }
      }
   }

   @Override
   public void addTargetEntity(AnchorDV anchor)
   {
      relationship.targetEntities.add(anchor);
   }

   @Override
   public void removeTargetEntity(AnchorDV anchor)
   {
      relationship.targetEntities.remove(anchor);
   }

   @Override
   public Future<String> execute()
   {
      Objects.requireNonNull(commitHook, "");

      return commitHook.apply(relationship);
   }

}
