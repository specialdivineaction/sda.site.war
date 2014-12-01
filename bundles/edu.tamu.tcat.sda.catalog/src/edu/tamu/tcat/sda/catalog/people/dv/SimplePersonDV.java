package edu.tamu.tcat.sda.catalog.people.dv;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SimplePersonDV
{
   public String id;
   public String syntheticName;
   public ArrayList<String> familyName;
   public ArrayList<String> displayName;
   public String birthLocation;
   public String deathLocation;
   public String summary;

   public String birthDate;
   public String deathDate;

//   public String _version_;

   public SimplePersonDV()
   {
   }

   public SimplePersonDV(PersonDV person)
   {
      id = person.id;

      for (PersonNameDV name : person.names)
      {
         familyName.add(name.familyName);
         displayName.add(name.displayName);
      }

      syntheticName = constructSyntheticName(person.names);
      birthLocation = person.birth.location;
      deathLocation = person.death.location;
      birthDate = person.birth.eventDate.toString();
      deathDate = person.death.eventDate.toString();
      summary = person.summary;
//      _version_ = null;
   }

   private String constructSyntheticName(Set<PersonNameDV> names)
   {
      Set<String> nameParts = new HashSet<>();
      for(PersonNameDV name : names)
      {
         nameParts.add(name.title);
         nameParts.add(name.givenName);
         nameParts.add(name.middleName);
         nameParts.add(name.familyName);
      }

      StringBuilder sb = new StringBuilder();
      for (String part : nameParts)
      {
         if (part == null)
            continue;

         sb.append(part).append(" ");
      }

      return sb.toString().trim();
   }
}
