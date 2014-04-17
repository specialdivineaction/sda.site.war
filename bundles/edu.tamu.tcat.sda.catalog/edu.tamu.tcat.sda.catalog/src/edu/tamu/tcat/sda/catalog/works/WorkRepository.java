package edu.tamu.tcat.sda.catalog.works;

import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.ds.DataStore;
import edu.tamu.tcat.sda.ds.DataUpdateObserver;

public interface WorkRepository extends DataStore
{
   Iterable<Work> listWorks();      // TODO must create a repeatable, pageable, identifiable result set.
   
   void create(WorkDV work, DataUpdateObserver<WorkDV, Work> observer);
   
   // TODO might return a handle that allows for cancellation, and blocking
   void update(WorkDV work, DataUpdateObserver<WorkDV, Work> observer);
}
