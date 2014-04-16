package edu.tamu.tcat.sda.catalog.psql;

import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkException;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

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
   public void create(WorkDV work) throws WorkException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void update(WorkDV work) throws WorkException
   {
      // TODO Auto-generated method stub
      
   }
}
