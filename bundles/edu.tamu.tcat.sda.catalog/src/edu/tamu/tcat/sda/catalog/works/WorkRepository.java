package edu.tamu.tcat.sda.catalog.works;

import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.datastore.DataStore;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public interface WorkRepository extends DataStore
{
   Iterable<Work> listWorks();      // TODO must create a repeatable, pageable, identifiable result set.

   Iterable<Work> listWorks(String title);
   /**
    * Given an author reference, return the biographical record for the referenced person.
    *
    * @param ref A reference to an author.
    * @return The person associated with that author.
    */
   Person getAuthor(AuthorReference ref);

   void create(WorkDV work, DataUpdateObserver<Work> observer);

   // TODO might return a handle that allows for cancellation, and blocking
   void update(WorkDV work, DataUpdateObserver<Work> observer);
}
