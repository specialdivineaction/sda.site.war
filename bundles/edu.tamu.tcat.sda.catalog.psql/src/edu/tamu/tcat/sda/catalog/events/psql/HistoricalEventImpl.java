package edu.tamu.tcat.sda.catalog.events.psql;

import java.util.Date;

import edu.tamu.tcat.sda.catalog.events.HistoricalEvent;
import edu.tamu.tcat.sda.catalog.events.dv.HistoricalEventDV;

public class HistoricalEventImpl implements HistoricalEvent
{

   private final String id;
   private final String title;
   private final String description;
   private final String location;
   private final Date start;

   public HistoricalEventImpl(HistoricalEventDV src)
   {
      this.id = src.id;
      this.title = src.title;
      this.description = src.description;
      this.location = src.location;
      this.start = src.eventDate;
   }
   
   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getTitle()
   {
      return title;
   }

   @Override
   public String getDescription()
   {
      return description;
   }

   @Override
   public String getLocation()
   {
      return location;
   }
   
   @Override
   public Date getDate()
   {
      return start;
   }


}
