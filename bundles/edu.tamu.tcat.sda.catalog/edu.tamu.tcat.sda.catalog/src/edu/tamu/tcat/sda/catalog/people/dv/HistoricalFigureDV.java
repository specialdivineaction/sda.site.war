package edu.tamu.tcat.sda.catalog.people.dv;

import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.events.dv.HistoricalEventDV;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.PersonName;

/**
 * Represents a Person 
 */
public class HistoricalFigureDV
{
   public String id;
   public Set<PersonNameDV> people;
   public HistoricalEventDV birth;
   public HistoricalEventDV death;
   
   public HistoricalFigureDV()
   {
      // TODO Auto-generated constructor stub
   }
   
   public HistoricalFigureDV(HistoricalFigure figure)
   {
      id = figure.getId();
      people = new HashSet<PersonNameDV>();
      for (PersonName n : figure.getAlternativeNames())
      {
         people.add(new PersonNameDV(n));
      }
      
      birth = new HistoricalEventDV(figure.getBirth());
      death = new HistoricalEventDV(figure.getDeath());
   }
   
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      
      for (PersonNameDV name : people)
      {
         if (name.displayName != null)
         {
            sb.append(name.displayName);
         }
         else 
         {
            String fn = name.familyName;
            String gn = name.givenName;
            if (fn != null && !fn.trim().isEmpty()) 
               sb.append(fn.trim());
            
            if (gn != null && !gn.trim().isEmpty())
            {
               if (sb.length() > 0)
                  sb.append(", ");
               
               sb.append(gn.trim());
            }
         }
         
         // TODO append dates
         
         break;
      }
      
      return sb.toString();
       
   }
   
}
