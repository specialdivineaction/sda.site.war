package edu.tamu.tcat.sda.catalog.relationship;

/**
 *  A service to access defined relationship types.
 */
public interface RelationshipTypeRegistry
{
   /**
    * Attempts to find and return the {@link RelationshipType} with the supplied
    * identifier.
    *
    * @param typeIdentifier The unique identifer of the {@code RelationshipType}
    *       to be retrieved.
    * @return The identified {@code RelationshipType}
    * @throws RelationshipException If the identified relationship type has not been
    *       registered with this registry.
    */
   RelationshipType resolve(String typeIdentifier) throws RelationshipException;
}
