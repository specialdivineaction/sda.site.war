package edu.tamu.tcat.sda.catalog.solr.test;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.SolrDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.catalogentries.relationship.solr.SolrQueryBuilder;

public class TestSolrQueryBuilder
{
   private static SolrServer solr;

   @BeforeClass
   public static void initSolrServer()
   {
      solr = new HttpSolrServer("http://localhost:8983/solr/HathiFiles");
   }

   @AfterClass
   public static void shutdown()
   {
      solr.shutdown();
   }

   @Test
   public void testQueryBuilder()
   {
      List<String> fieldNames = new ArrayList<>();
      fieldNames.add("source");
      fieldNames.add("access");

      Date startDate = Date.valueOf("1700-01-01");
      Date endDate = Date.valueOf("1800-01-01");

      Integer rowsToReturn = 3;
      Integer countinueFrom = 0;

      SolrQuery sqBuilder = new SolrQueryBuilder.Builder()
                                   .newQuery()
                                   .setQueryString("title","\"Gilbert West\"")
                                   .addFacetFields(fieldNames)
                                   .addFacetRange("publicationDate", startDate, endDate, "+5YEARS")
                                   .setNumRows(rowsToReturn.intValue())
                                   .setStartRows(countinueFrom.intValue())
                                   .addHighLighting("title")
                                   .setSort("publicationDate", ORDER.asc)
                                   .build()
                                   .getQuery();

      try
      {
         QueryResponse response = solr.query(sqBuilder);
         Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
         for(Entry<String, Map<String, List<String>>> highlight : highlighting.entrySet())
         {
            for(Entry<String, List<String>> entry : highlight.getValue().entrySet())
            {
               System.out.println("Key:" + entry.getKey() + "\nValues:");
               for(String value : entry.getValue())
               {
                  System.out.println(value + "\n");
               }
            }
         }
         for(RangeFacet<Date, String> range : response.getFacetRanges())
         {
            for(RangeFacet.Count count : range.getCounts())
            {
               System.out.println(count.getValue() + ":" + count.getCount());
            }
         }
         for(FacetField facetField : response.getFacetFields())
         {
            List<Count> values = facetField.getValues();
            for(Count value : values)
            {
               System.out.println(value.getName() + ":"  + value.getCount());
            }

         }
         for (SolrDocument document : response.getResults())
         {
            for (String fieldName : document.getFieldNames())
            {
               System.out.println(fieldName + " | " + document.getFieldValue(fieldName));
            }
         }
      }
      catch (SolrServerException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}
