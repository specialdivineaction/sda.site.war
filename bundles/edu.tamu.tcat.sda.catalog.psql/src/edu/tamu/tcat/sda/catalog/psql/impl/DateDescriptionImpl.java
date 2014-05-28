package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.Date;

import edu.tamu.tcat.sda.catalog.works.DateDescription;
import edu.tamu.tcat.sda.catalog.works.dv.DateDescriptionDV;

public class DateDescriptionImpl implements DateDescription
{

   private final DateDescriptionDV descript;
   
   public DateDescriptionImpl(DateDescriptionDV descript)
   {
      this.descript = descript;
   }
   
   @Override
   public String getDisplay()
   {
      return this.descript.display;
   }

   @Override
   public Date getValue()
   {
      return this.descript.value;
   }

}
