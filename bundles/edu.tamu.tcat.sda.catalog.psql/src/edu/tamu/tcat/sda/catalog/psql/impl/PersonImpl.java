package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.catalogentries.events.HistoricalEvent;
import edu.tamu.tcat.catalogentries.people.Person;
import edu.tamu.tcat.catalogentries.people.PersonName;
import edu.tamu.tcat.catalogentries.people.dv.PersonDV;
import edu.tamu.tcat.catalogentries.people.dv.PersonNameDV;
import edu.tamu.tcat.sda.catalog.events.psql.HistoricalEventImpl;

public class PersonImpl implements Person
{
   private final String id;
   private final PersonName canonicalName;
   private final Set<PersonName> names;
   private final HistoricalEventImpl birth;
   private final HistoricalEventImpl death;
   private final String summary;

   public PersonImpl(PersonDV figure)
   {
      id = figure.id;
      canonicalName = getCanonicalName(figure);
      names = new HashSet<PersonName>();
      for (PersonNameDV n : figure.names)
      {
         names.add(new PersonNameImpl(n));
      }

      birth = new HistoricalEventImpl(figure.birth);
      death = new HistoricalEventImpl(figure.death);
      summary = figure.summary;
   }

   /**
    * Get a canonical name from a person DV. Prefer to use the displayName field, but fall back to
    * the first element in the 'names' set if a displayName is not available.
    *
    * @param figure
    * @return canonical name for this person
    */
   private static PersonName getCanonicalName(PersonDV figure)
   {
      // try the 'displayName' first
      if (figure.displayName != null) {
         return new PersonNameImpl(figure.displayName);
      }

      // fall back to using the first element of the 'names' set
      if (!figure.names.isEmpty()) {
         PersonNameDV nameDV = figure.names.iterator().next();
         return new PersonNameImpl(nameDV);
      }

      // fall back to "Name Unknown" if this person does not have any names
      PersonNameDV fallbackName = new PersonNameDV();
      fallbackName.displayName = "Name Unknown";
      return new PersonNameImpl(fallbackName);
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public PersonName getCanonicalName()
   {
      return canonicalName;
   }

   @Override
   public Set<PersonName> getAlternativeNames()
   {
      return Collections.unmodifiableSet(names);
   }

   @Override
   public HistoricalEvent getBirth()
   {
      return birth;
   }

   @Override
   public HistoricalEvent getDeath()
   {
      return death;
   }

   @Override
   public String getSummary()
   {
      return summary;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      // use canonical name for display purposes
      PersonName name = canonicalName;

      // fall back to first element of names
      if (name == null && !names.isEmpty()) {
         name = names.iterator().next();
      }

      if (name != null) {
         if (name.getDisplayName() != null)
         {
            sb.append(name.getDisplayName());
         }
         else
         {
            String fn = name.getFamilyName();
            String gn = name.getGivenName();
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
      }

      return sb.toString();
   }

   // equals and hash code?

}