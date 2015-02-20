package edu.tamu.tcat.sda.catalog.relationship;

import java.net.URI;
import java.util.Collection;

public interface RelationshipQueryCommand
{

   public abstract Collection<Relationship> getResults();

   public abstract RelationshipQueryCommand forEntity(URI entity, RelationshipDirection direction);

   public abstract RelationshipQueryCommand forEntity(URI entity);

   public abstract RelationshipQueryCommand byType(String typeId);

   public abstract RelationshipQueryCommand setRowLimit(int rows);

   // TODO: decide on the proper way to organize the results
   public abstract RelationshipQueryCommand oderBy();

}
