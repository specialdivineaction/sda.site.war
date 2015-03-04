package edu.tamu.tcat.trc.entries.bib.solr;

import java.util.Collection;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.bib.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.bib.dto.TitleDV;

public class WorkDocumentBuilder
{

   private SolrInputDocument document;
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
   private final static String docSeries = "series";
   private final static String docSummary = "summary";

   private final static String editionId = "editionId";
   private final static String editionName = "editionName";

   private final static String volumeId = "volumeId";
   private final static String volumeNumber = "volumeNumber";

   WorkDocumentBuilder()
   {
      document = new SolrInputDocument();
   }

   public SolrInputDocument getDocument()
   {
      return document;
   }

   void addDocumentId(String id)
   {
      document.addField(workId, id);
   }

   void addEditionId(String id)
   {
      document.addField(editionId, id);
   }

   void addEditionName(String name)
   {
      document.addField(editionName, name);
   }

   void addVolumeId(String id)
   {
      document.addField(volumeId, id);
   }

   void addVolumeNumber(String number)
   {
      document.addField(volumeNumber, number);
   }

   void addAuthors(List<AuthorRefDV> authors)
   {
      for (AuthorRefDV author : authors)
      {
         if (author.authorId != null)
            document.addField(authorIds, author.authorId);
         else
            document.addField(authorIds, "");
         document.addField(authorNames, author.name);
         document.addField(authorRoles, author.role);
      }
   }

   void addTitle(Collection<TitleDV> titlesDV)
   {
      for (TitleDV title : titlesDV)
      {
         document.addField(titleTypes, title.type);
         document.addField(language, title.lg);
         document.addField(titles, title.title);
         document.addField(subtitles, title.subtitle);
      }
   }

   void addPublication(PublicationInfoDV publication)
   {
      if (publication.publisher != null)
         document.addField(publisher, publication.publisher);
      else
         document.addField(publisher, "");
      if (publication.place != null)
         document.addField(pubLocation, publication.place);
      else
         document.addField(pubLocation, "");

      DateDescriptionDV dateDescription = publication.date;
      document.addField(pubDateString, dateDescription.description);

      if (dateDescription.calendar != null)
         document.addField(pubDateValue, convertDate(dateDescription.calendar));
   }

   void addSeries(String series)
   {
      document.addField(docSeries, series);
   }

   void addSummary(String summary)
   {
      document.addField(docSummary, summary);
   }

   private String convertDate(String localDate)
   {
      return localDate + "T00:00:00Z";
   }
}
