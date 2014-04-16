package edu.tamu.tcat.sda.catalog.works;

import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.ds.DataStore;

public interface WorkRepository extends DataStore
{
   Iterable<Work> listWorks();      // TODO must create a repeatable, pageable, identifiable result set.
   
   void create(WorkDV work) throws WorkException;
   
   void update(WorkDV work) throws WorkException;
}
