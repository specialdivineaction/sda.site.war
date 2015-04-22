package edu.tamu.tcat.trc.entries.bio.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.catalogentries.events.dv.HistoricalEventDV;
import edu.tamu.tcat.trc.entries.bio.dv.PersonDV;
import edu.tamu.tcat.trc.entries.bio.dv.PersonNameDV;
import edu.tamu.tcat.trc.entries.bio.dv.SimplePersonDV;

public class AuthorController
{
   // FIXME this needs to be named correctly.
   Logger log = Logger.getLogger("edu.tamu.tcat.catalogentries.solr.authorcontroller");
   private final SolrServer solr;

   private final static String personId = "id";
   private final static String familyName = "familyName";
   private final static String syntheticName = "syntheticName";
   private final static String displayName = "displayName";
   private final static String birthLocation = "birthLocation";
   private final static String birthDate = "birthDate";
   private final static String deathLocation = "deathLocation";
   private final static String deathDate = "deathDate";
   private final static String summary = "summary";

   private final static String numResults = "numResults";

   private String collectionName = "authors";
   private String rootSolrEndpoint = "https://sda-dev.citd.tamu.edu/solr/";

   public AuthorController()
   {
      solr = new HttpSolrServer(rootSolrEndpoint + collectionName);
   }

   private SolrQuery buildQuery(MultivaluedMap<String, String> queryParams)
   {
      SolrQuery query = new SolrQuery();

      for (String key : queryParams.keySet())
      {
         String first = queryParams.getFirst(key);
         switch (key)
         {
            case familyName:
               query.setQuery(syntheticName + ":*" + first + "*");
               break;
            case birthLocation:
               query.setQuery(birthLocation + ":*" + first + "*");
               break;
            case deathLocation:
               query.setQuery(deathLocation + ":*" + first + "*");
               break;
            case birthDate:
               query.setQuery(birthDate + ":*" + first + "*");
               break;
            case deathDate:
               query.setQuery(deathDate + ":*" + first + "*");
               break;
            case numResults:
               query.setRows(Integer.valueOf(first));
               break;
            default:
               query.setQuery(syntheticName + ":*" + first + "*");
               break;

         }
      }
      if (queryParams.isEmpty())
      {
         query.setQuery(syntheticName + ":*");
//         query.setQuery(summary + ":*");
      }
      return query;
   }

   public List<SimplePersonDV> query(MultivaluedMap<String, String> queryParams)
   {

      try
      {
         QueryResponse response = solr.query(buildQuery(queryParams));
         SolrDocumentList results = response.getResults();
         List<SimplePersonDV> spList = new ArrayList<SimplePersonDV>();
         for (SolrDocument result : results)
         {
            SimplePersonDV simplePerson = new SimplePersonDV();
            Collection<String> fieldNames = result.getFieldNames();
//            String string = result.getFieldValueMap().toString();
//            sp.add(mapper.fromJSON(string, new JsonTypeReference<SimplePersonDV>(){}));
            for (String fieldName : fieldNames)
            {
               switch(fieldName)
               {
                  case "id":
                     simplePerson.id = result.getFieldValue(fieldName).toString();
                     break;
                  case "syntheticName":
                     simplePerson.syntheticName = result.getFieldValue(fieldName).toString();
                     break;
                  case "familyName":
                     simplePerson.familyName = (ArrayList<String>)result.getFieldValue(fieldName);
                     break;
                  case "displayName":
                     simplePerson.displayName = (ArrayList<String>)result.getFieldValue(fieldName);
                     break;
                  case "birthLocation":
                     simplePerson.birthLocation = result.getFieldValue(fieldName).toString();
                     break;
                  case "birthDate":
                     simplePerson.birthDate = result.getFieldValue(fieldName).toString();
                     break;
                  case "deathLocation":
                     simplePerson.deathLocation = result.getFieldValue(fieldName).toString();
                     break;
                  case "deathDate":
                     simplePerson.deathDate = result.getFieldValue(fieldName).toString();
                     break;
                  case "summary":
                     simplePerson.summary = result.getFieldValue(fieldName).toString();
                     break;

               }
            }

            spList.add(simplePerson);

         }
         return spList;
      }
      catch (SolrServerException e)
      {
         log.severe("An error occured with Solr Server:" + e);
      }
      return null;
   }

   private String guardNull(String value)
   {
      return value == null ? "" : value;
   }

   private String convertDate(DateDescriptionDV date)
   {
      return date.calendar + "T00:00:00Z";
   }

//   <T> SolrDocumentAdapter<T> getAdapter(Class<T> type) throws UnsupportedTypeException
//   {
//      // lookup from some registry
//
//      return null;
//   }

   /**
    * Adapts the supplied data vehicle into a {@link SolrInputDocument}.
    *
    * @param person The data to bee added to the database.
    * @return The document to index.
    */
   private SolrInputDocument adapt(PersonDV person)
   {
      SolrInputDocument document = new SolrInputDocument();

      document.addField(personId, person.id);
      document.addField(syntheticName, constructSyntheticName(person.names));
      for(PersonNameDV name : person.names)
      {
         document.addField(familyName, guardNull(name.familyName));
         document.addField(displayName, guardNull(name.displayName));
      }

      HistoricalEventDV birth = person.birth;
      document.addField(birthLocation, guardNull(birth.location));
      DateDescriptionDV bDate = birth.date;
      if (bDate != null)
         document.addField(birthDate, convertDate(bDate));

      HistoricalEventDV death = person.birth;
      document.addField(deathLocation, guardNull(death.location));
      if (death.date != null)
         document.addField(deathDate, convertDate(death.date));

      document.addField(summary, guardNull(person.summary));

      // TODO need default display name
      //      maybe first sentence of summary
      // 'defualtDisplayName' : Neal Audenaert (1978 - )
      // First sentence of summary
      return document;
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

   public void addDocument(PersonDV author)
   {
      SolrInputDocument document = adapt(author);

      try
      {
         solr.add(document);
         solr.commit();
      }
      catch (IOException e)
      {
         log.severe("An error occured in the transmition of the document:" + e);
      }
      catch (SolrServerException e)
      {
         log.severe("An error occured with Solr Server:" + e);
      }
   }

   public void clean()
   {
      try
      {
         solr.deleteByQuery("*:*");
      }
      catch (IOException e)
      {
         log.severe("An error occured in the transmition of the document:" + e);
      }
      catch (SolrServerException e)
      {
         log.severe("An error occured with Solr Server:" + e);
      }
   }

   public void reindex()
   {

   }
}
