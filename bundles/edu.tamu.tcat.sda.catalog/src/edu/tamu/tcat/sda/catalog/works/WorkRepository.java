package edu.tamu.tcat.sda.catalog.works;

import edu.tamu.tcat.sda.catalog.CommandExecutionListener;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.datastore.DataStore;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

/**
 * Provides the main point of access for creating, editing and querying works.
 *
 */
public interface WorkRepository extends DataStore
{
   /**
    *
    * @return An {@link Iterable} over all works in the collection.
    * @deprecated This method will be replaced or updated in order to prevent unbounded
    *    retrieval of all items. In general, we should provide a robust query mechanism
    *    rather than a 'get all'. We also need to support a repeatable, pageable,
    *    identifiable result set.
    */
   @Deprecated
   Iterable<Work> listWorks();      // TODO must create a repeatable, pageable, identifiable result set.

   /**
    *
    * @param title The title to search for.
    * @return All works whose title partially matches the supplied title.
    * @deprecated To be replaced with a more robust query/search mechanism.
    *
    */
   @Deprecated
   Iterable<Work> listWorks(String title);

   /**
    * @param workId The id of the work to retrieve.
    * @return The record for a specific work.
    * @throws NoSuchCatalogRecordException If the requested work does not exist.
    */
   Work getWork(int workId) throws NoSuchCatalogRecordException;     // FIXME should be a string valued id

   /**
    * Given an author reference, return the biographical record for the referenced person.
    *
    * @param ref A reference to an author.
    * @return The person associated with that author.
    */
   Person getAuthor(AuthorReference ref);

   /**
    * Construct an {@link EditWorkCommand} to be used to modify an existing work.
    *
    * @param id
    * @return
    * @throws NoSuchCatalogRecordException
    */
   EditWorkCommand edit(String id) throws NoSuchCatalogRecordException;

   EditWorkCommand create();

   AutoCloseable addBeforeUpdateListener(CommandExecutionListener ears);

   AutoCloseable addAfterUpdateListener(CommandExecutionListener ears);

   @Deprecated // use the EditWorkCommand methods
   void create(WorkDV work, DataUpdateObserver<Work> observer);

   // TODO might return a handle that allows for cancellation, and blocking
   @Deprecated // use the EditWorkCommand methods
   void update(WorkDV work, DataUpdateObserver<Work> observer);
}
