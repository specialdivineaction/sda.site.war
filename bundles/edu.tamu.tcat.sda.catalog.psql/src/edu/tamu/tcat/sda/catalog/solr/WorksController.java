package edu.tamu.tcat.sda.catalog.solr;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.DateDescriptionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class WorksController
{

   Logger log = Logger.getLogger("edu.tamu.tcat.sda.catalog.solr.workcontroller");
   private final String solrBaseUri = "https://sda-dev.citd.tamu.edu/solr/";
   private final SolrServer solr;

   // Field Name values
   private final static String workId = "id";
   private final static String authorIds = "authorIds";
   private final static String authorNames = "authorNames";
   private final static String authorRoles = "authorRole";
   private final static String titleTypes = "titleTypes";
   private final static String language = "lang";
   private final static String titles = "titles";
   private final static String subtitles = "subtitles";
   private final static String publisher = "publisher";
   private final static String pubLocatcion = "publisherLocation";
   private final static String pubDateString = "publishDateString";
   private final static String pubDateValue = "publishDateValue";
   private final static String series = "series";
   private final static String summary = "summary";


   public WorksController()
   {
      solr = new HttpSolrServer(solrBaseUri + "works");
   }

   public void addDocument(WorkDV work)
   {
      SolrInputDocument document = new SolrInputDocument();

      document.addField(workId, work.id);

      for (AuthorRefDV author : work.authors)
      {
         document.addField(authorIds, author.authorId);
         document.addField(authorNames, author.name);
         document.addField(authorRoles, author.role);
      }

      for (TitleDV title : work.titles)
      {
         document.addField(titleTypes, title.type);
         document.addField(language, title.lg);
         document.addField(titles, title.title);
         document.addField(subtitles, title.subtitle);
      }

      PublicationInfoDV publication = work.pubInfo;
      document.addField(publisher, publication.publisher);
      document.addField(pubLocatcion, publication.place);

      DateDescriptionDV dateDescription = publication.date;
      document.addField(pubDateString, dateDescription.display);
      document.addField(pubDateValue, convertDate(dateDescription.value));

      document.addField(series, work.series);
      document.addField(summary, work.summary);

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
