package edu.tamu.tcat.trc.entries.bib.copy.hathitrust;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.bib.copy.ResourceAccessException;
import edu.tamu.tcat.trc.entries.bib.copy.discovery.ContentQuery;
import edu.tamu.tcat.trc.entries.bib.copy.discovery.CopySearchResult;
import edu.tamu.tcat.trc.entries.bib.copy.discovery.CopySearchService;

/**
 *  Searches over the local SOLR index of Hathifiles data. Note that this index may be expanded
 *  to incorporate resource that are not hosted by Hathi Trust, depending on future design
 *  decisions.
 *
 *  Intended to be registered and configured as an OSGi service.
 */
public class HTFilesSearchService implements CopySearchService
{
   private static final Logger logger = Logger.getLogger(HTFilesSearchService.class.getName());
   private static final int MAX_ROWS = 100;

   private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_INSTANT;

   private HttpSolrServer solrServer;

   private ConfigurationProperties props;

   public void setConfiguration(ConfigurationProperties props)
   {
      this.props = props;
   }

   public void activate()
   {
      Objects.requireNonNull(props, "Cannot connect to Solr Server. Configuration data is not available.");

      URI solrEndpoint = props.getPropertyValue("solr.api.endpoint", URI.class);
      String core = props.getPropertyValue("hathifiles", String.class);
      solrServer = new HttpSolrServer(solrEndpoint.resolve(core).toString());

      // check to ensure that the requested SolrServer can be contacted.
      // NOTE: may not be the right place for this
      try
      {
         SolrPingResponse pingResponse = solrServer.ping();
         if (pingResponse == null || pingResponse.getStatus() > 299 || pingResponse.getStatus() < 200)
            throw new IllegalStateException("Failed to ping configured solr server [" + solrEndpoint.resolve(core) + "]: " + pingResponse);
      }
      catch (IOException | SolrServerException ex)
      {
         throw new IllegalStateException("Failed to ping configured solr server [" + solrEndpoint.resolve(core) + "]", ex);
      }
   }

   public void deactivate()
   {
      if (solrServer != null)
      {
         try
         {
            solrServer.shutdown();
         }
         catch (Exception ex)
         {
            logger.log(Level.SEVERE, "Failed to shut down connection to solr server", ex);
         }

         solrServer = null;
      }
   }

   @Override
   public CopySearchResult find(ContentQuery query) throws ResourceAccessException
   {
      // HACK hard coded. Should be provided to the service
      try
      {
         Objects.requireNonNull(solrServer, "No active connection to Solr Server");

         // TODO build query
         String queryString = formatQueryString(query);


         SolrQuery solrQuery = new SolrQuery(queryString);

         QueryResponse response = solrServer.query(solrQuery);
         // DO SOMETHING.

      }
      catch (Exception ex)
      {
         throw new ResourceAccessException("", ex);
      }


      throw new UnsupportedOperationException();
   }

   private String trimToNull(String str)
   {
      return (str == null || str.trim().isEmpty()) ? null : str.trim();
   }

   private String formatQueryString(ContentQuery query) throws UnsupportedEncodingException
   {
      StringBuilder qBuilder = new StringBuilder();

      buildMainQuery(query, qBuilder);
      buildDateFilter(query, qBuilder);
      buildPagingFilter(query, qBuilder);

      return qBuilder.toString();
   }

   private void buildMainQuery(ContentQuery query, StringBuilder qBuilder) throws UnsupportedEncodingException
   {
      // TODO sanitize filter - remove &'s, etc.

      // append the required query value
      String keyWordQuery = trimToNull(query.getKeyWordQuery());
      if (keyWordQuery == null)
         throw new IllegalArgumentException("Invalid copy query [" + query + "]. No keyword query supplied.");

      // add author query info
      String authorQ = trimToNull(query.getAuthorQuery());
      if (authorQ != null)
      {
         // HathiTrust records this in the title field, if at all, so append to keywords.
         keyWordQuery += " " + authorQ;
      }

      //         ?q=title%3A(essay+hume)&fq=publicationDate%3A%5B1700-01-01T00%3A00%3A00Z+TO+1800-01-01T00%3A00%3A00Z%5D
      keyWordQuery = URLEncoder.encode(keyWordQuery, "UTF-8");    // UTF-8 required by standard
      qBuilder.append("q=title").append(URLEncoder.encode(":", "UTF-8"))
      .append("(").append(keyWordQuery).append(")");
   }

   private void buildDateFilter(ContentQuery query, StringBuilder qBuilder) throws UnsupportedEncodingException
   {
      // add filter for date range.
      //fq=publicationDate%3A%5B1700-01-01T00%3A00%3A00Z+TO+1800-01-01T00%3A00%3A00Z%5D

      TemporalAccessor rangeStart = query.getDateRangeStart();
      TemporalAccessor rangeEnd = query.getDateRangeEnd();
      if (rangeStart != null || rangeEnd != null)
      {
         String start = (rangeStart == null) ? "*": dateFormatter.format(rangeStart) + "/YEAR";
         String end = (rangeEnd == null) ? "*": dateFormatter.format(rangeEnd) + "/YEAR";

         qBuilder.append("&fq=publicationDate")
                 .append(URLEncoder.encode(":[", "UTF-8"))
                 .append(URLEncoder.encode(start, "UTF-8"))
                 .append("+TO+")
                 .append(URLEncoder.encode(end, "UTF-8"))
                 .append(URLEncoder.encode("]", "UTF-8"));
      }
   }

   private void buildPagingFilter(ContentQuery query, StringBuilder qBuilder)
   {
      int offset = query.getOffset();
      if (offset < 0)
         qBuilder.append("&start=").append(offset);

      int limit = query.getLimit();
      qBuilder.append("&rows=").append(Math.min(limit, MAX_ROWS));
   }


}
