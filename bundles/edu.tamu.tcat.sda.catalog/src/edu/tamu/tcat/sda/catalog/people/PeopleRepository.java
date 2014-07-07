package edu.tamu.tcat.sda.catalog.people;

import edu.tamu.tcat.sda.catalog.CatalogRepoException;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

/**
 *
 *
 */
public interface PeopleRepository
{
   /**
    * Retrieves all people in the collection.
    * @return An iterable over all people in the repo.
    * @throws CatalogRepoException
    * @deprecated pending a rewrite to a more extensible filtering mechanism. In general, "select *"
    *       APIs should be avoided.
    */
   @Deprecated
   Iterable<Person> findPeople() throws CatalogRepoException;

   /**
    * @param prefix The first few letters of this person's last name.
    * @return An iterable of all matching names.
    * @deprecated Will be replaced by a more robust filtering mechanism ASAP.
    */
   @Deprecated
   Iterable<Person> findByName(String prefix) throws CatalogRepoException;

   /**
    * Retrieves a person by their long identifier.
    *
    * @param personId
    * @return
    */
   @Deprecated
   Person getPerson(long personId) throws CatalogRepoException, NoSuchCatalogRecordException;

   /**
    * Retrieves a person instance by their string identifier.
    * @param personId
    * @return
    */
   Person getPerson(String personId) throws NoSuchCatalogRecordException;

   /**
    * Creates a new entry for the supplied historical figure. Note that no de-duplication will
    * be performed. If this person (or a similar person) has already been added, a new entry
    * will be created.
    *
    * <p>This method will execute asynchronously. Upon success, it will pass an instance of
    * {@link Person} representing the create person to the observer. On failure, it
    * will supply an error message and optionally, a exception associated with the failure.
    *
    * @param histFigure A data vehicle containing the information for the person to create.
    * @param observer An optional observer that will be notified upon success or failure of
    *       this operation.
    */
   void create(PersonDV histFigure, DataUpdateObserver<Person> observer);

   /**
    * Updates the entry for the supplied historical figure. Note that this assumes that the
    * supplied person has already been created, that is, {@link #getPerson(String)} returns
    * successfully for {@code histFigure.id}.
    *
    * <p>This method will execute asynchronously. Upon success, it will pass an instance of
    * {@link Person} representing the updated person to the observer. On failure, it
    * will supply an error message and optionally, a exception associated with the failure.
    *
    * @param histFigure A data vehicle containing the information for the person to update.
    * @param observer An optional observer that will be notified upon success or failure of
    *       this operation.
    */
   void update(PersonDV histFigure, DataUpdateObserver<Person> observer);

   /**
    * Marks the entry for the identified person as having been deleted. References to this
    * person will be retained for historical and data consistency purposes but will not be
    * accessible via standard interfaces and queries.
    *
    * @param personId The unique identifier of the person to delete.
    * @param observer An optional observer that will be notified upon success or failure of
    *       this operation. Note that in the case of deletion, failure will result in an
    *       exception, while successful deletion will be indicated by a call to
    *       {@link DataUpdateObserver#finish(Object)} with a {@code null} result object.
    */
   void delete(String personId, DataUpdateObserver<Void> observer);
}