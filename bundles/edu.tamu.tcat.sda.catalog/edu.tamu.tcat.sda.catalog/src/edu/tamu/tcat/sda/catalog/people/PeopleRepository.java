package edu.tamu.tcat.sda.catalog.people;

import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

/**
 *
 *
 */
public interface PeopleRepository
{
   Iterable<Person> listHistoricalFigures();

   // FIXME change to string
   Person getPerson(long personId);

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