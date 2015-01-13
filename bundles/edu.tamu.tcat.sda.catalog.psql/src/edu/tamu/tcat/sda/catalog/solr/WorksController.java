package edu.tamu.tcat.sda.catalog.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;
import edu.tamu.tcat.sda.catalog.works.dv.SimpleWorkDV;
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class WorksController
{

   Logger log = Logger.getLogger("edu.tamu.tcat.sda.catalog.solr.workcontroller");
   private final String solrBaseUri = "https://sda-dev.citd.tamu.edu/solr/";
   private final SolrServer solr;

   // Field Name values for works
   private final static String workId = "id";
   private final static String editionId = "editionId";
   private final static String editionName = "editionName";
   private final static String volumeId = "volumeId";
   private final static String vNumber = "volumeNumber";
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
      if (queryParams.isEmpty())
         query.setQuery("*:*");

      // Sort order can not be done on an arraylist

      return query;
   }

   @SuppressWarnings("unchecked")
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
                  case "workId":
                     simpleWork.id = result.getFieldValue(fieldName).toString();
                     break;
                  case "authorIds":
                     simpleWork.authorIds = (List<String>)result.getFieldValue(fieldName);
                     break;
                  case "authorNames":
                     simpleWork.authorNames = (List<String>)result.getFieldValue(fieldName);
                     break;
                  case "authorRole":
                     simpleWork.authorRole = (List<String>)result.getFieldValue(fieldName);
                     break;
                  case "titleTypes":
                     simpleWork.titleTypes = (List<String>)result.getFieldValue(fieldName);
                     break;
                  case "lang":
                     simpleWork.lang = (List<String>)result.getFieldValue(fieldName);
                     break;
                  case "titles":
                     simpleWork.titles = (List<String>)result.getFieldValue(fieldName);
                     break;
                  case "subtitles":
                     simpleWork.subtitles = (List<String>)result.getFieldValue(fieldName);
                     break;
                  case "series":
                     simpleWork.series = result.getFieldValue(fieldName).toString();
                     break;
                  case "summary":
                     simpleWork.summary = result.getFieldValue(fieldName).toString();
                     break;
                  default:
                     // ignore unknown fields
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
      Collection<SolrInputDocument> documents = new HashSet<>();
      WorkDocumentBuilder workDocument = new WorkDocumentBuilder();

      workDocument.addDocumentId(work.id);
      workDocument.addAuthors(work.authors);
      workDocument.addTitle(work.titles);
      workDocument.addSeries(work.series);
      workDocument.addSummary(work.summary);
      documents.add(workDocument.getDocument());
      if (!work.editions.isEmpty())
      {
         WorkDocumentBuilder editionDocument = new WorkDocumentBuilder();
         for (EditionDV edition : work.editions)
         {
            appendEdition(work, documents, editionDocument, edition);
         }
      }

      try
      {
         solr.add(documents);
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

   private void appendEdition(WorkDV work, Collection<SolrInputDocument> documents, WorkDocumentBuilder editionDocument, EditionDV edition)
   {
      editionDocument.addDocumentId(work.id + ":" + edition.id);
//            editionDocument.addEditionId(edition.id);
      editionDocument.addEditionName(edition.editionName);
      editionDocument.addAuthors(edition.authors);
      editionDocument.addTitle(edition.titles);
      editionDocument.addPublication(edition.publicationInfo);
      editionDocument.addSeries(edition.series);
      editionDocument.addSummary(edition.summary);
      documents.add(editionDocument.getDocument());

      if(!edition.volumes.isEmpty())
      {
         appendVolume(work, documents, edition);
      }
   }

   private void appendVolume(WorkDV work, Collection<SolrInputDocument> documents, EditionDV edition)
   {
      for (VolumeDV volume : edition.volumes)
      {
         // HACK this is a document builder, not a document
         WorkDocumentBuilder volumeDocument = new WorkDocumentBuilder();

         volumeDocument.addDocumentId(work.id + ":" + edition.id + ":" + volume.id);
//       volumeDocument.addEditionId(edition.id);
         volumeDocument.addEditionName(edition.editionName);
//       volumeDocument.addVolumeId(volume.id);
         volumeDocument.addVolumeNumber(volume.volumeNumber);
         volumeDocument.addAuthors(volume.authors);
         volumeDocument.addTitle(volume.titles);
         volumeDocument.addPublication(edition.publicationInfo); // Is there not a volume Publisher?
         volumeDocument.addSeries(volume.series);
         volumeDocument.addSummary(volume.summary);

         documents.add(volumeDocument.getDocument());
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