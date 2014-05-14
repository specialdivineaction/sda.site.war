package edu.tamu.tcat.sda.catalog.people;

import java.util.List;

import edu.tamu.tcat.sda.catalog.works.HistoricalEvent;

public interface Person
{
   PersonNames getCanonical();
   
   List<PersonNames> getNames();
   
   HistoricalEvent getBirth();
   
   HistoricalEvent getDeath();
}
