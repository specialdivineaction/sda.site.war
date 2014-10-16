package edu.tamu.tcat.sda.catalog.solr;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.sda.catalog.events.dv.HistoricalEventDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameDV;

public class AuthorController
{

   Logger log = Logger.getLogger("edu.tamu.tcat.sda.catalog.solr.authorcontroller");
   private final String solrBaseUri = "https://sda-dev.citd.tamu.edu/solr/";
   private final SolrServer solr;

   private final static String authorId = "id";
   private final static String authorTitle = "title";
   private final static String givenName = "givenName";
   private final static String middleName = "middleName";
   private final static String familyName = "familyName";
   private final static String suffix = "suffix";
   private final static String displayName = "displayName";
   private final static String birthTitle = "bTitle";
   private final static String birthDescription = "bDescript";
   private final static String birthLocation = "bLocation";
   private final static String birthDate = "bDate";
   private final static String deathTitle = "dTitle";
   private final static String deathDescription = "dDescript";
   private final static String deathLocation = "dLocation";
   private final static String deathDate = "dDate";
   private final static String summary = "summary";

   public AuthorController()
   {
      solr = new HttpSolrServer(solrBaseUri + "authors");
   }

   public void addDocument(PersonDV author)
   {
      SolrInputDocument document = new SolrInputDocument();


      document.addField(authorId, author.id);

      Set<PersonNameDV> authors = author.names;
      for(PersonNameDV authorDV : authors)
      {
         document.addField(authorTitle, authorDV.title == null ? "" : authorDV.title);
         document.addField(givenName, authorDV.givenName == null ? "" : authorDV.givenName);
         document.addField(middleName, authorDV.middleName == null ? "" : authorDV.middleName);
         document.addField(familyName, authorDV.familyName == null ? "" : authorDV.familyName);
         document.addField(suffix, authorDV.suffix == null ? "" : authorDV.suffix);
         document.addField(displayName,  authorDV.displayName == null ? "" : authorDV.displayName);
      }

      HistoricalEventDV birth = author.birth;
      document.addField(birthTitle, birth.title == null ? "" : birth.title);
      document.addField(birthDescription, birth.description == null ? "" : birth.description);
      document.addField(birthLocation, birth.location == null ? "" : birth.location);
      Date bDate = birth.eventDate;
      if (bDate != null)
         document.addField(birthDate, convertDate(birth.eventDate));

      HistoricalEventDV death = author.birth;
      document.addField(deathTitle, death.title == null ? "" : death.title);
      document.addField(deathDescription, death.description == null ? "" : death.description);
      document.addField(deathLocation, death.location == null ? "" : death.location);
      if (death.eventDate != null)
         document.addField(deathDate, convertDate(death.eventDate));

      document.addField(summary, author.summary == null ? "" : author.summary);

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

   private String convertDate(Date event)
   {
      String dateRep = "";

      SimpleDateFormat calendar = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat time = new SimpleDateFormat("HH:mm:SS");
      dateRep = calendar.format(event) + "T" + time.format(event) + "Z";

      return dateRep;
   }
}
