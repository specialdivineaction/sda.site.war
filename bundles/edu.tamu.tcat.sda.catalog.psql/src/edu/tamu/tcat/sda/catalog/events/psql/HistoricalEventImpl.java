package edu.tamu.tcat.sda.catalog.events.psql;

import edu.tamu.tcat.sda.catalog.events.DateDescription;
import edu.tamu.tcat.sda.catalog.events.HistoricalEvent;
import edu.tamu.tcat.sda.catalog.events.dv.DateDescriptionDV;
import edu.tamu.tcat.sda.catalog.events.dv.HistoricalEventDV;

public class HistoricalEventImpl implements HistoricalEvent
{
   private final String id;
   private final String title;
   private final String description;
   private final String location;
   private final DateDescription eventDate;

   public HistoricalEventImpl(HistoricalEventDV src)
   {
      this.id = src.id;
      this.title = src.title;
      this.description = src.description;
      this.location = src.location;
      this.eventDate = DateDescriptionDV.convert(src.date);
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
   public DateDescription getDate()
   {
      return eventDate;
   }


}
