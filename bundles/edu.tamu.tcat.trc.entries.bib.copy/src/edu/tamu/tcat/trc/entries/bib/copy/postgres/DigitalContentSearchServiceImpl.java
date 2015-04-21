package edu.tamu.tcat.trc.entries.bib.copy.postgres;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import edu.tamu.tcat.hathitrust.bibliography.Record;
import edu.tamu.tcat.sda.catalog.relationships.search.solr.SolrQueryBuilder;
import edu.tamu.tcat.trc.entries.bib.copy.legacy.DigitalContentReference;
import edu.tamu.tcat.trc.entries.bib.copy.legacy.DigitalContentReferenceDTO;
import edu.tamu.tcat.trc.entries.bib.copy.legacy.DigitalContentSearchCommand;
import edu.tamu.tcat.trc.entries.bib.copy.legacy.DigitalContentSearchService;
import edu.tamu.tcat.trc.entries.bib.copy.legacy.DigitalCopyProvider;

public class DigitalContentSearchServiceImpl implements DigitalContentSearchService
{
   private final Logger logger = Logger.getLogger("edu.tamu.tcat.trc.entries.bib.copy.postgres");
   private Collection<DigitalContentReference> digitalContent;
   private String searchString;

   private void searchHathiTrust()
   {
      SolrServer solr = new HttpSolrServer("http://localhost:8983/solr/HathiFiles");
      SolrQuery sqBuilder = (new SolrQueryBuilder())
                                                // TODO: Modify search string to be optimized for this particular provider.
                                                .setQueryString("title", searchString)
   //                                             .addFacetFields(fieldNames)
   //                                             .addFacetRange("publicationDate", startDate, endDate, "+5YEARS")
   //                                             .setNumRows(rowsToReturn.intValue())
   //                                             .setStartRows(countinueFrom.intValue())
                                                .addHighLighting("title")
                                                .setSort("publicationDate", ORDER.asc)
                                                .build();
      try
      {
         QueryResponse response = solr.query(sqBuilder);
         for (SolrDocument document : response.getResults())
         {
            DigitalContentReferenceDTO digitalContentRef = new DigitalContentReferenceDTO();
            digitalContentRef.provider = DigitalCopyProvider.HathiTrust;

            digitalContentRef.recordNumber = document.getFieldValue("recordNumber").toString();
            digitalContentRef.access = document.getFieldValue("access").toString();
            digitalContentRef.rights = document.getFieldValue("rights").toString();
            digitalContentRef.source = document.getFieldValue("source").toString();
            digitalContentRef.sourceRecordNumber = document.getFieldValue("sourceRecordNumber").toString();
            digitalContentRef.title = document.getFieldValue("title").toString();

            digitalContent.add(new HathiTrustDigitalContentImpl(digitalContentRef));
         }
      }
      catch (SolrServerException e)
      {
         logger.log(Level.FINE, "An error occured while sending a query to SOLR." + "/n Search String: " + searchString + "/n" + e);
      }
   }

   public Collection<DigitalContentReference> getSearchResults()
   {
      return digitalContent;
   }

   @Override
   public Collection<DigitalContentReference> searchForDigitalContent(String query)
   {
      this.digitalContent = new HashSet<>();
      this.searchString = query;
      searchHathiTrust();
      return digitalContent;
   }

   @Override
   public DigitalContentSearchCommand createQueryString()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Collection<Record> getBibligraphicRecords(DigitalCopyProvider provider, String recordNumber)
   {
      Collection<Record> records = new HashSet<>();
      DigitalContentSearchCommandImpl digitalContentCommand = new DigitalContentSearchCommandImpl();

      switch (provider)
      {
         case HathiTrust:
            records.addAll(digitalContentCommand.getHathiTrustContent(recordNumber));
            break;
         default:
            break;

      }
      return records;
   }

   @Override
   public DigitalContentSearchCommand createRequest()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
