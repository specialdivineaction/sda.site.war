package edu.tamu.tcat.sda.catalog.relationship;

public interface RelationshipTypeRegistry
{
   RelationshipType resolve(String typeIdentifier);
}
