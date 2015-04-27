package edu.tamu.tcat.trc.entries.bib.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;

import edu.tamu.tcat.trc.entries.bib.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.bib.dto.WorkInfo;

public class WorkSolrQueryCommand implements WorkQueryCommand
{
   private final static Logger logger = Logger.getLogger(WorkQueryCommand.class.getName());

   // Solr field name values for works

   private SolrQuery query = new SolrQuery();
   private Collection<String> criteria = new ArrayList<>();

   private SolrServer solr;

   public WorkSolrQueryCommand(SolrServer solr)
   {
      this.solr = solr;
   }

   @Override
   public Collection<WorkInfo> getResults()
   {
      Collection<WorkInfo> works = new HashSet<>();
      String workInfo = null;
      WorkInfo wi = new WorkInfo();
      QueryResponse response;

      try
      {
         response = solr.query(getQuery());
         SolrDocumentList results = response.getResults();

         for (SolrDocument doc : results)
         {
            try
            {
               workInfo = doc.getFieldValue("workInfo").toString();
               wi = WorksIndexingService.mapper.readValue(workInfo, WorkInfo.class);
               works.add(wi);
            }
            catch (IOException ioe)
            {
               logger.log(Level.SEVERE, "Failed to parse relationship record: [" + workInfo + "]. " + ioe);
            }
         }
      }
      catch(SolrServerException e)
      {
         logger.log(Level.SEVERE, "The following error occurred while querying the works core :" + e);
      }

      return works;
   }

   private SolrParams getQuery()
   {
//      String queryString = Joiner.on(" AND ").join(criteria);
//      query.setQuery(queryString);
      return query;
   }

   @Override
   public WorkQueryCommand searchWorks(String title)
   {
      criteria.add("titles\"" + title + "\"");
      return this;
   }

   @Override
   public WorkQueryCommand byAuthor(String authorName)
   {
      criteria.add("authorNames\"" + authorName + "\"");
      return this;
   }

   @Override
   public WorkQueryCommand byPublishedDate(Date publishedDate)
   {
      // TODO Auto-generated method stub
      return this;
   }

   @Override
   public WorkQueryCommand byPublishedLocation(String location)
   {
      criteria.add("publisherLocation\"" + location + "\"");
      return this;
   }
}
