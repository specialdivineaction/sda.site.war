package edu.tamu.tcat.sda.catalog.solr;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.sda.catalog.events.dv.HistoricalEventDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameDV;

public class ManageDocuments
{

   private final String solrBaseUri = "https://sda-dev.citd.tamu.edu/solr/";

   public ManageDocuments()
   {
   }

   public void addAuthorDocument(PersonDV author)
   {
      SolrServer solr = new HttpSolrServer(solrBaseUri + "authors");
      SolrInputDocument document = new SolrInputDocument();


      document.addField("id", author.id);

      Set<PersonNameDV> authors = author.names;
      for(PersonNameDV authorDV : authors)
      {
         document.addField("title", isNull(authorDV.title) ? "" : authorDV.title);
         document.addField("givenName", isNull(authorDV.givenName) ? "" : authorDV.givenName);
         document.addField("middleName", isNull(authorDV.middleName) ? "" : authorDV.middleName);
         document.addField("familyName", isNull(authorDV.familyName) ? "" : authorDV.familyName);
         document.addField("suffix", isNull(authorDV.suffix) ? "" : authorDV.suffix);
         document.addField("displayName", isNull(authorDV.displayName) ? "" : authorDV.displayName);
      }

      DateFormat df = DateFormat.getDateInstance();
      String format = "2014-01-01T23:00:00Z";
      HistoricalEventDV birth = author.birth;
      document.addField("birthId", isNull(birth.id) ? "" : birth.id);
      document.addField("bTitle", isNull(birth.title) ? "" : birth.title);
      document.addField("bDescript", isNull(birth.description) ? "" : birth.description);
      document.addField("bLocation", isNull(birth.location) ? "" : birth.location);
      Date bDate = birth.eventDate;
      if (bDate == null)
         document.addField("bDate", format);
      else
         document.addField("bDate", convertDate(birth.eventDate));

      HistoricalEventDV death = author.birth;
      document.addField("deathId", isNull(death.id) ? "" : death.id);
      document.addField("dTitle", isNull(death.title) ? "" : death.title);
      document.addField("dDescript", isNull(death.description) ? "" : death.description);
      document.addField("dLocation", isNull(death.location) ? "" : death.location);
      if (death.eventDate == null)
         document.addField("dDate", format);
      else
         document.addField("dDate", convertDate(death.eventDate));


      document.addField("summary", isNull(author.summary) ? "" : author.summary);

      try
      {

         UpdateResponse response = solr.add(document);
         solr.commit();
      }
      catch (IOException e)
      {

      }
      catch (SolrServerException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private Boolean isNull(String field)
   {
      if(field == null)
         return true;
      return false;

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
