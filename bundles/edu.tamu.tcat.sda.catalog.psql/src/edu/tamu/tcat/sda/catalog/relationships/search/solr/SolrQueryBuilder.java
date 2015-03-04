package edu.tamu.tcat.sda.catalog.relationships.search.solr;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.params.HighlightParams;

// TODO this seems to be getting used as a general purpose query builder.
public class SolrQueryBuilder
{
   // TODO design and test
   // TODO Support:
   //      serializable, repeatable, identifiable queries
   //      result caching
   //      auto-paging
   //      define valid fields for this query at construction
   //      more robust definition of fields


   private SolrQuery query = new SolrQuery();
   private int start = 0;
   private int limit = 10;

   // TODO: We may want to separate this out depending on how the query params come through.
   public SolrQueryBuilder setQueryString(String fieldName, String param)
   {
      // TODO: if quotes are used in the search, then the result is exclusive
      query.setQuery(fieldName + ":" + param);
      return this;
   }

   /**
    * Set the starting point of the next set of documents to be returned, default is 0.
    * @param begin
    */
   public SolrQueryBuilder setStart(int begin)
   {
      if (begin < 0)
         throw new IllegalArgumentException("Start value must be greater than or equal to zero");

      this.start = begin;
      query.setStart(Integer.valueOf(start));      // TODO move to build step
      return this;
   }

   /**
    * @param limit The maximum number of items to be returned.
    * @throws IllegalArgumentException If the supplied limit is less than 0;
    */
   public SolrQueryBuilder setLimit(int limit)
   {
      if (limit < 0)
         throw new IllegalArgumentException("Limit value must be greater than or equal to zero");

      this.limit = limit;
      query.setRows(Integer.valueOf(limit));       // TODO move to build step
      return this;
   }

   private static class FacetField
   {
      public enum SortType { index, count, undefined };

      public enum FacetMethod { enumerate, fc, fcs }

      /** The name of the field this describes */
      public String fieldName;
      /** The minimum number of items that must be present for a facet to be returned. */
      public int minCount;
      /** Indicates whether and how the facat values should be sorted. */
      public SortType sort = SortType.undefined;
      /** The maximum number of facet values that should be returned */
      public int limit = Integer.MIN_VALUE;
      public int offset = Integer.MIN_VALUE;
      public FacetMethod method = null;
      public int numThreads = Integer.MIN_VALUE;
      public String prefix;
      public boolean missing;

      public String buildQuery()
      {
         throw new UnsupportedOperationException();
      }
   }

   Set<FacetField> facets = new HashSet<>();
   /**
    * Enables the ability to facet multiple fields.
    * @param fieldNames - Determines which field(s) a facet should be calculated upon.
    * @param minCount - Sets a minimum number of documents in which a facet value must appear
    *                   before it will be returned, default is set to 1
    */
   public SolrQueryBuilder addFacetFields(String fieldNames, int minCount)
   {
      FacetField field = new FacetField();
      field.fieldName = fieldNames;
      field.minCount = minCount;

      // TODO build facet queries in #build()
      return this;

//      facets.add(e)
//      query.setFacet(true);
//      query.setFacetMinCount(minCount > 1 ? minCount : 1);
//      query.addFacetField(fieldNames);
//      query.addFacetQuery(f)
//      return this;
   }

   /**
    * Adds the ability to search multiple fields.
    * @param fieldNames - Determines which field(s) a facet should be calculated upon.
    */
   public SolrQueryBuilder addFacetFields(String[] fieldNames)
   {
      // TODO set global facet field count?
      Arrays.stream(fieldNames).forEach(field -> addFacetFields(field, 1));
      return this;
   }

   /**
    * Added the ability to return a facet for a date range
    * @param fieldName - The field a range facet should be calculated on.
    * @param start - Specifies the lower bound of the date range.
    * @param end - Specifies the upper bound of the date range.
    * @param gap - The size of each range. Example(+1DAY, +2MONTHS, +5YEARS, etc...)
    */
   public SolrQueryBuilder addFacetRange(String fieldName, Date start, Date end, String gap)
   {
      query.addDateRangeFacet(fieldName, start, end, gap);
      return this;
   }

   /**
    * Enables the ability to return highlighted results from the specified field.
    * @param fieldName
    */
   public SolrQueryBuilder addHighLighting(String fieldName)
   {
      query.setHighlight(true);
      query.set(HighlightParams.FIELDS, fieldName);
      return this;
   }

   /**
    * Set the ability to sort on a given fieldName.
    * @param fieldName
    * @param order
    */
   public SolrQueryBuilder setSort(String fieldName, ORDER order)
   {
      query.setSort(fieldName, order);
      return this;
   }

   public SolrQuery build()
   {
      return query;
   }


}
