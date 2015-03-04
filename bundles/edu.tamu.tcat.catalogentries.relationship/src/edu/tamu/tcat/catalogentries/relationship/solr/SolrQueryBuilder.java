package edu.tamu.tcat.catalogentries.relationship.solr;

import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.HighlightParams;

public class SolrQueryBuilder
{
   //Fields
   private SolrQuery query;


   public static class Builder
   {
      private SolrQuery builderQuery;
      // methods returning Builder instantiating fields
      public Builder newQuery()
      {
         builderQuery = new SolrQuery();
         return this;
      }

      // TODO: We may want to separate this out depending on how the query params come through.
      public Builder setQueryString(String fieldName, String param)
      {
         // TODO: if quotes are used in the search, then the result is exclusive
         builderQuery.setQuery(fieldName + ":" + param);
         return this;
      }

      /**
       * Sets the maximum number of documents to be returned.
       * @param rows
       */
      public Builder setNumRows(Integer rows)
      {
         builderQuery.setRows(rows);
         return this;
      }

      /**
       * Set the starting point of the next set of documents to be returned, default is 0.
       * @param begin
       */
      public Builder setStartRows(Integer begin)
      {
         builderQuery.setStart(begin > 0 ? begin : 0);
         return this;
      }

      /**
       * Enables the ability to facet multiple fields.
       * @param fieldNames - Determines which field(s) a facet should be calculated upon.
       * @param minCount - Sets a minimum number of documents in which a facet value must appear
       *                   before it will be returned, default is set to 1
       */
      public Builder addFacetFields(List<String> fieldNames, Integer minCount)
      {
         builderQuery.setFacet(true);
         builderQuery.setFacetMinCount(minCount > 1 ? minCount : 1);

         // HACK: need to find a better way to add N number of field names. If this method is called more
         //       the one time, the last Facet_Field to be set is the only one.
         builderQuery.set(FacetParams.FACET_FIELD, fieldNames.get(0), fieldNames.get(1));
         return this;
      }

      /**
       * Adds the ability to search multiple fields.
       * @param fieldNames - Determines which field(s) a facet should be calculated upon.
       */
      public Builder addFacetFields(List<String> fieldNames)
      {
         int minCount = 1;
         return addFacetFields(fieldNames, minCount);
      }

      /**
       * Added the ability to return a facet for a date range
       * @param fieldName - The field a range facet should be calculated on.
       * @param start - Specifies the lower bound of the date range.
       * @param end - Specifies the upper bound of the date range.
       * @param gap - The size of each range. Example(+1DAY, +2MONTHS, +5YEARS, etc...)
       */
      public Builder addFacetRange(String fieldName, Date start, Date end, String gap)
      {
         builderQuery.addDateRangeFacet(fieldName, start, end, gap);
         return this;
      }

      /**
       * Enables the ability to return highlighted results from the specified field.
       * @param fieldName
       */
      public Builder addHighLighting(String fieldName)
      {
         builderQuery.setHighlight(true);
         builderQuery.set(HighlightParams.FIELDS, fieldName);
         return this;
      }

      /**
       * Set the ability to sort on a given fieldName.
       * @param fieldName
       * @param order
       */
      public Builder setSort(String fieldName, ORDER order)
      {
         builderQuery.setSort(fieldName, order);
         return this;
      }

      public SolrQueryBuilder build()
      {
         return new SolrQueryBuilder(this);
      }
   }

   private SolrQueryBuilder(Builder builder)
   {
      this.query = builder.builderQuery;
   }

   public SolrQuery getQuery()
   {
      return query;
   }
}
