package edu.tamu.tcat.sda.catalog.works;

import java.util.List;

public interface Person
{
   PersonNames getCanonical();
   
   List<PersonNames> getNames();
   
   SimpleEvent getBirth();
   
   SimpleEvent getDeath();
}
