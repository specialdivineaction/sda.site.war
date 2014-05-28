package edu.tamu.tcat.sda.catalog.works.dv;

import java.util.Date;

import edu.tamu.tcat.sda.catalog.works.DateDescription;

/**
 * Defines a date as represented by user-entered values. The primary representation of 
 * this date is a string-valued display (eg. March 2004, CMXXIII). This display is 
 * supplemented with a machine readable version of the data suitable for use in situating 
 * this date on a timeline or comparing it to other temporal objects. Note the machine 
 * readable version is optional and may be approximate. 
 */
public class DateDescriptionDV
{
   // TODO better semantics for fuzzy historical dates
   public String display;
   public Date value;
   
   public DateDescriptionDV()
   {
   }
   
   public DateDescriptionDV(DateDescription descript)
   {
      this.display = descript.getDisplay();
      this.value = descript.getValue();
   }
}
