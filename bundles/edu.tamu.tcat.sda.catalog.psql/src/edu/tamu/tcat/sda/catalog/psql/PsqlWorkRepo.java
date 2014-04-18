package edu.tamu.tcat.sda.catalog.psql;

import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.ds.DataUpdateObserver;

public class PsqlWorkRepo implements WorkRepository
{
   // TODO should we use something like the data source executor service?

   @Override
   public Iterable<Work> listWorks()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void create(WorkDV work, DataUpdateObserver<WorkDV, Work> observer)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void update(WorkDV work, DataUpdateObserver<WorkDV, Work> observer)
   {
      // TODO Auto-generated method stub
      
   }
}
