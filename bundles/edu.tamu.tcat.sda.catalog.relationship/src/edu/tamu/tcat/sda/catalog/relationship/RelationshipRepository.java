package edu.tamu.tcat.sda.catalog.relationship;

import edu.tamu.tcat.sda.datastore.DataStore;


/**
 * Manages the persistence of {@link Relationship}s and other entities within the
 * relationship framework.
 *
 * <p>
 * Note that this is intended to serve as a thin facade to the underlying storage system.
 * It implements minimal data integrity checks and provides hooks for listening to data
 * persistence events (e.g., to support externally configurable logging and audit trails).
 * The repository provides basic data integrity checks but is not intended to enforce
 * business logic. It is also not responsible for performing authentication or authorization.
 * These functions are the responsibility of the client application that configures and uses
 * the repository.
 *
 */
public interface RelationshipRepository extends DataStore
{
   /**
    * @param id The id of the relationship to retrieve.
    * @return The relationship with the provided id.
    *
    * @throws RelationshipNotAvailableException If there is no relationship with the supplied id.
    * @throws RelationshipPersistenceException If there are errors accessing the persistence layer.
    */
   Relationship get(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException;

   // Relationship get(URI relnUri);

   /**
    *  Construct an {@link EditRelationshipCommand} for use in creating a new relationship.
    *
    *  @return An {@link EditRelationshipCommand} for use in updating the properties
    *       of the relationship to be created.
    *  @throws RelationshipPersistenceException If a new command instance could not be
    *       created.
    */
   EditRelationshipCommand create() throws RelationshipPersistenceException;

   /**
    * Construct an {@link EditRelationshipCommand} to modify an existing relationship.
    *
    * @param id The id of the {@link Relationship} to edit.
    * @return An {@code EditRelationshipCommand} for use in modifying the
    *       identified {@code Relationship}.
    * @throws RelationshipNotAvailableException If the identified relationship does not exist.
    * @throws RelationshipPersistenceException If there are errors accessing the persistence layer.
    */
   EditRelationshipCommand edit(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException;

   /**
    * Delete an existing {@link Relationship}.
    *
    * @param id The id of the {@link Relationship} to delete.
    * @throws RelationshipNotAvailableException If the identified relationship does not exist
    * @throws RelationshipPersistenceException If there are errors accessing the persistence layer.
    */
   void delete(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException;

//
//   AutoCloseable addBeforeUpdateListener(CommandExecutionListener ears);
//
//   AutoCloseable addAfterUpdateListener(CommandExecutionListener ears);

   // TODO provide support for listener system
   // TODO support the creation/mgnt of defined sets of relationships
   // TODO support tracking the history of revisions to relationships
   // NOTE these might be separate services
}
