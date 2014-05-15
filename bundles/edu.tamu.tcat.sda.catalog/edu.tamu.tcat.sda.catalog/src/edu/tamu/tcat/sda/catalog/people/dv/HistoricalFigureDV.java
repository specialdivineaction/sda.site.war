package edu.tamu.tcat.sda.catalog.people.dv;

import java.util.List;

/**
 * Represents a Person 
 */
public class HistoricalFigureDV
{
   public String id;
   public List<PersonNameRefDV> people;
   public List<HistoricalEventDV> events;
   
}
