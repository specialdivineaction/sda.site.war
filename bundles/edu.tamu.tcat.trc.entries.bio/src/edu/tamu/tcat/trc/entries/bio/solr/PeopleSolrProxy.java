package edu.tamu.tcat.trc.entries.bio.solr;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.catalogentries.events.dv.HistoricalEventDV;
import edu.tamu.tcat.trc.entries.bio.Person;
import edu.tamu.tcat.trc.entries.bio.dv.PersonDV;
import edu.tamu.tcat.trc.entries.bio.dv.PersonNameDV;

public class PeopleSolrProxy
{
   private final static String personId = "id";
   private final static String familyName = "familyName";
   private final static String syntheticName = "syntheticName";
   private final static String displayName = "displayName";
   private final static String birthLocation = "birthLocation";
   private final static String birthDate = "birthDate";
   private final static String deathLocation = "deathLocation";
   private final static String deathDate = "deathDate";
   private final static String summary = "summary";

   private static SolrInputDocument document;
   private Map<String,Object> fieldModifier;
   private final static String SET = "set";

   public PeopleSolrProxy()
   {
      document = new SolrInputDocument();
   }

   public static SolrInputDocument getDocument()
   {
      return document;
   }

   public static PeopleSolrProxy createPerson(Person person)
   {
      PeopleSolrProxy proxy = new PeopleSolrProxy();
      PersonDV personDV = new PersonDV(person);
      Set<PersonNameDV> names = personDV.names;

      document.addField(personId, personDV.id);
      document.addField(syntheticName, constructSyntheticName(personDV.names));
      for(PersonNameDV name : personDV.names)
      {
         document.addField(familyName, guardNull(name.familyName));
         document.addField(displayName, guardNull(name.displayName));
      }

      HistoricalEventDV birth = personDV.birth;
      document.addField(birthLocation, guardNull(birth.location));
      DateDescriptionDV bDate = birth.date;
      if (bDate != null)
         document.addField(birthDate, convertDate(bDate));

      HistoricalEventDV death = personDV.birth;
      document.addField(deathLocation, guardNull(death.location));
      if (death.date != null)
         document.addField(deathDate, convertDate(death.date));

      document.addField(summary, guardNull(personDV.summary));

      return proxy;
   }



   private static String guardNull(String value)
   {
      return value == null ? "" : value;
   }

   private static String convertDate(DateDescriptionDV date)
   {
      return date.calendar + "T00:00:00Z";
   }

   /**
    * Constructs a synthetic name that contains the various values (title, first name,
    * family name, etc) from different names associated with this person. Each portion
    * of a person's name is collected into a set of 'name parts' that is then concatenated
    * to form a string-valued synthetic name. This allows all of the various name tokens to
    * be included in the search.
    *
    * @param names A set of names associated with a person.
    * @return A synthetic name that contains a union of the different name fields.
    */
   private static String constructSyntheticName(Set<PersonNameDV> names)
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
