package edu.tamu.tcat.sda.catalog.solr;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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

import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.DateDescriptionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.SimpleWorkDV;
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
   private final static String pubLocation = "publisherLocation";
   private final static String pubDateString = "publishDateString";
   private final static String pubDateValue = "publishDateValue";
   private final static String series = "series";
   private final static String summary = "summary";

   private final static String numResults = "numResults";

   public WorksController()
   {
      solr = new HttpSolrServer(solrBaseUri + "works");
   }

   private SolrQuery buildQuery(MultivaluedMap<String, String> queryParams)
   {
      SolrQuery query = new SolrQuery();

      for (String key : queryParams.keySet())
      {
         String first = queryParams.getFirst(key);
         switch (key)
         {
            case titles:
               query.setQuery(titles + ":*" + first + "*");
               break;
            case publisher:
               query.setQuery(publisher + ":*" + first + "*");
               break;
            case pubLocation:
               query.setQuery(pubLocation + ":*" + first + "*");
               break;
            case pubDateString:
               query.setQuery(pubDateString + ":*" + first + "*");
               break;
            case pubDateValue:
               query.setQuery(pubDateValue + ":*" + first + "*");
               break;
            case series:
               query.setQuery(series + ":*" + first + "*");
               break;
            case summary:
               query.setQuery(summary + ":*" + first + "*");
               break;
            case authorNames:
               query.setQuery(authorNames + ":*" + first + "*");
               break;
            case language:
               query.setQuery(language + ":*" + first + "*");
               break;
            case numResults:
               query.setRows(Integer.parseInt(first));
               break;
            default:
               query.setQuery(titles +  ":*" + first + "*");
               break;

         }
      }

      // Sort order can not be done on an arraylist

      return query;
   }

   public List<SimpleWorkDV> query(MultivaluedMap<String, String> queryParams) throws JsonException
   {

      try
      {
         QueryResponse response = solr.query(buildQuery(queryParams));
         SolrDocumentList results = response.getResults();
         List<SimpleWorkDV> swList = new ArrayList<SimpleWorkDV>();
         for (SolrDocument result : results)
         {
            SimpleWorkDV simpleWork = new SimpleWorkDV();
            Collection<String> fieldNames = result.getFieldNames();
//            String string = result.getFieldValueMap().toString();
//            sp.add(mapper.fromJSON(string, new JsonTypeReference<SimpleWorkDV>(){}));

            for (String fieldName : fieldNames)
            {
               switch(fieldName)
               {
                  case "id":
                     simpleWork.id = result.getFieldValue(fieldName).toString();
                     break;
                  case "authorIds":
                     simpleWork.authorIds = (ArrayList<String>)result.getFieldValue(fieldName);
                     break;
                  case "authorNames":
                     simpleWork.authorNames = (ArrayList<String>)result.getFieldValue(fieldName);
                     break;
                  case "authorRole":
                     simpleWork.authorRole = (ArrayList<String>)result.getFieldValue(fieldName);
                     break;
                  case "titleTypes":
                     simpleWork.titleTypes = (ArrayList<String>)result.getFieldValue(fieldName);
                     break;
                  case "lang":
                     simpleWork.lang = (ArrayList<String>)result.getFieldValue(fieldName);
                     break;
                  case "titles":
                     simpleWork.titles = (ArrayList<String>)result.getFieldValue(fieldName);
                     break;
                  case "subtitles":
                     simpleWork.subtitles = (ArrayList<String>)result.getFieldValue(fieldName);
                     break;
                  case "publisher":
                     simpleWork.publisher = result.getFieldValue(fieldName).toString();
                     break;
                  case "publisherLocation":
                     simpleWork.publisherLocation = result.getFieldValue(fieldName).toString();
                     break;
                  case "publishDateString":
                     simpleWork.publishDateString = result.getFieldValue(fieldName).toString();
                     break;
                  case "publishDateValue":
                     simpleWork.publishDateValue = result.getFieldValue(fieldName).toString();
                     break;
                  case "series":
                     simpleWork.series = result.getFieldValue(fieldName).toString();
                     break;
                  case "summary":
                     simpleWork.summary = result.getFieldValue(fieldName).toString();
                     break;

               }
            }

            swList.add(simpleWork);

         }
         return swList;
      }
      catch (SolrServerException e)
      {
         log.severe("An error occured with Solr Server:" + e);
      }
      return null;
   }

   public void addDocument(WorkDV work)
   {
      SolrInputDocument document = new SolrInputDocument();

      document.addField(workId, work.id);

      for (AuthorRefDV author : work.authors)
      {
         if (author.authorId != null)
            document.addField(authorIds, author.authorId);
         else
            document.addField(authorIds, "");
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
      document.addField(pubLocation, publication.place);

      DateDescriptionDV dateDescription = publication.date;
      document.addField(pubDateString, dateDescription.display);

      if (dateDescription.value != null)
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
