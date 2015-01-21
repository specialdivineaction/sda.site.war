package edu.tamu.tcat.sda.catalog.people.dv;

import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.events.dv.HistoricalEventDV;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.people.PersonName;

/**
 * Represents a Person
 */
public class PersonDV
{
   public String id;
   public PersonNameDV displayName;
   public Set<PersonNameDV> names;
   public HistoricalEventDV birth;
   public HistoricalEventDV death;
   public String summary;

   public PersonDV()
   {
      // TODO Auto-generated constructor stub
   }

   public PersonDV(Person figure)
   {
      id = figure.getId();

      PersonName canonicalName = figure.getCanonicalName();
      if (canonicalName != null) {
         displayName = new PersonNameDV(canonicalName);
      }

      names = new HashSet<PersonNameDV>();
      for (PersonName n : figure.getAlternativeNames())
      {
         names.add(new PersonNameDV(n));
      }

      birth = new HistoricalEventDV(figure.getBirth());
      death = new HistoricalEventDV(figure.getDeath());
      summary = figure.getSummary();
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      for (PersonNameDV name : names)
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
