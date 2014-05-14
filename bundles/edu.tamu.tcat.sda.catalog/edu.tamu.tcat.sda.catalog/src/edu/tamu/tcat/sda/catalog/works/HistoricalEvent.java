package edu.tamu.tcat.sda.catalog.works;

import java.util.Date;

public interface HistoricalEvent
{
   Date getDate();
   
   String getLocation();
   
   String getDescription();
   
   String getTitle();
}
