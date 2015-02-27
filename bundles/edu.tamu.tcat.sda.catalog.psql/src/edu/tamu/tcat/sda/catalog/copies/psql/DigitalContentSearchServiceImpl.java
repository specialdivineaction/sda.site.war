package edu.tamu.tcat.sda.catalog.copies.psql;

import java.util.Collection;
import java.util.HashSet;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.sda.catalog.copies.DigitalContentReference;
import edu.tamu.tcat.sda.catalog.copies.DigitalContentReferenceDTO;
import edu.tamu.tcat.sda.catalog.copies.DigitalContentSearchCommand;
import edu.tamu.tcat.sda.catalog.copies.DigitalContentSearchService;
import edu.tamu.tcat.sda.catalog.copies.DigitalCopyProvider;
import edu.tamu.tcat.sda.catalog.relationships.search.solr.SolrQueryBuilder;

public class DigitalContentSearchServiceImpl implements DigitalContentSearchService
{
   private Collection<DigitalContentReference> digitalContent;
   private String searchString;

   private void searchHathiTrust()
   {
      SolrServer solr = new HttpSolrServer("http://localhost:8983/solr/HathiFiles");
      SolrQuery sqBuilder = new SolrQueryBuilder.Builder()
                                                .newQuery()
                                                // TODO: Modify search string to be optimized for this particular provider.
                                                .setQueryString("title", searchString)
   //                                             .addFacetFields(fieldNames)
   //                                             .addFacetRange("publicationDate", startDate, endDate, "+5YEARS")
   //                                             .setNumRows(rowsToReturn.intValue())
   //                                             .setStartRows(countinueFrom.intValue())
                                                .addHighLighting("title")
                                                .setSort("publicationDate", ORDER.asc)
                                                .build()
                                                .getQuery();
      try
      {
         QueryResponse response = solr.query(sqBuilder);
         for (SolrDocument document : response.getResults())
         {
            DigitalContentReferenceDTO digitalContentRef = new DigitalContentReferenceDTO();
            digitalContentRef.provider = DigitalCopyProvider.HathiTrust;
            for(String fieldName : document.getFieldNames())
            {
               switch(fieldName)
               {
                  case "recordNumber":
                     digitalContentRef.recordNumber = document.getFieldValue(fieldName).toString();
                     break;
                  case "access":
                     digitalContentRef.access = document.getFieldValue(fieldName).toString();
                     break;
                  case "rights":
                     digitalContentRef.rights = document.getFieldValue(fieldName).toString();
                     break;
                  case "source":
                     digitalContentRef.source= document.getFieldValue(fieldName).toString();
                     break;
                  case "sourceRecordNumber":
                     digitalContentRef.sourceRecordNumber = document.getFieldValue(fieldName).toString();
                     break;
                  case "title":
                     digitalContentRef.title = document.getFieldValue(fieldName).toString();
                     break;
                  default:
                     break;
               }
            }
            digitalContent.add(new HathiTrustDigitalContentImpl(digitalContentRef));
         }
      }
      catch (SolrServerException e)
      {
         e.printStackTrace();
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

      switch(provider.toString())
      {
         case "HahtiTrust":
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
