package edu.tamu.tcat.sda.catalog.events.dv;

import java.util.Date;

import edu.tamu.tcat.sda.catalog.events.HistoricalEvent;

public class HistoricalEventDV
{
   public String id;
   public String title;
   public String description;
   public String location;
   public Date eventDate;
   
   public HistoricalEventDV()
   {
      
   }
   
   public HistoricalEventDV(HistoricalEvent orig)
   {
      this.id = orig.getId();
      this.title = orig.getTitle();
      this.description = orig.getDescription();
      this.location = orig.getLocation();
      this.eventDate = orig.getDate();
   }
}
