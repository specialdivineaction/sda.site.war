package edu.tamu.tcat.sda.catalog.people.dv;

import java.util.Date;
import java.util.Set;

/**
 * Represents a Person 
 */
public class HistoricalFigureDV
{
   public String id;
   public Set<PersonNameDV> people;
   public Date birth;
   public Date death;
   
}
