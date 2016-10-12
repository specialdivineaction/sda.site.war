package edu.tamu.tcat.sda.rest.search.v1;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.article.impl.search.ArticleSearchStrategy;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.BibliographicSearchStrategy;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.WorkSolrQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.search.SearchWorksResult;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.BioSearchStrategy;
import edu.tamu.tcat.trc.entries.types.bio.search.BioEntryQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.search.PersonSearchResult;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

public class UnifiedSearchResource
{
   private static final Logger logger = Logger.getLogger(UnifiedSearchResource.PeopleSearchDelegate.class.getName());

   private final SearchServiceManager searchServiceMgr;
   private final ConfigurationProperties config;

   private final WorkSearchDelegate workSearchDelegate;
   private final PeopleSearchDelegate peopleSearchDelegate;
   private final ArticleSearchDelegate articleSearchDelegate;

   public UnifiedSearchResource(SearchServiceManager searchServiceMgr, ConfigurationProperties config)
   {
      this.searchServiceMgr = searchServiceMgr;
      this.config = config;

      this.workSearchDelegate = new WorkSearchDelegate();
      this.peopleSearchDelegate = new PeopleSearchDelegate();
      this.articleSearchDelegate = new ArticleSearchDelegate();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.UnifiedResult search(@QueryParam("q") String query,
                                         @QueryParam(value = "off") @DefaultValue("0")   int offset,
                                         @QueryParam(value = "max") @DefaultValue("100") int numResults)
   {
      SearchWorksResult works = workSearchDelegate.search(query, offset, numResults);
      PersonSearchResult people = peopleSearchDelegate.search(query, offset, numResults);
      ArticleSearchResult articles = articleSearchDelegate.search(query, offset, numResults);

      RestApiV1.UnifiedResult dto = new RestApiV1.UnifiedResult();
      dto.query = query;
      dto.offset = offset;
      dto.max = numResults;
      dto.works.clear();
      dto.works.addAll(SearchAdapter.adapt(works));
      dto.people.clear();
      dto.people.addAll(SearchAdapter.adapt(people));
      dto.articles.clear();
      dto.articles.addAll(SearchAdapter.adapt(articles));

      return dto;
   }

   private class WorkSearchDelegate
   {
      private final QueryService<WorkSolrQueryCommand> queryService;

      public WorkSearchDelegate()
      {
         BibliographicSearchStrategy searchStrategy = new BibliographicSearchStrategy();
         queryService = searchServiceMgr.getQueryService(searchStrategy);
      }

      public SearchWorksResult search(String query, int offset, int numResults)
      {
         WorkQueryCommand queryCommand = queryService.createQuery();
         queryCommand.query(query);
         queryCommand.setOffset(offset);
         queryCommand.setMaxResults(numResults);
         return queryCommand.execute();
      }
   }

   private class PeopleSearchDelegate
   {
      private final QueryService<BioEntryQueryCommand> queryService;

      public PeopleSearchDelegate()
      {
         BioSearchStrategy searchStrategy = new BioSearchStrategy(config);
         queryService = searchServiceMgr.getQueryService(searchStrategy);
      }

      public PersonSearchResult search(String query, int offset, int numResults)
      {
         BioEntryQueryCommand queryCommand = queryService.createQuery();
         queryCommand.query(query);
         queryCommand.setOffset(offset);
         queryCommand.setMaxResults(numResults);
         CompletableFuture<PersonSearchResult> future = queryCommand.execute();

         try
         {
            return future.get(10, TimeUnit.SECONDS);
         }
         catch (InterruptedException | ExecutionException | TimeoutException e)
         {
            Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
            String msg = "Failed to get search results for people";
            logger.log(Level.SEVERE, msg, cause);
            throw new WebApplicationException(cause, Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build());
         }
      }
   }

   private class ArticleSearchDelegate
   {
      private final QueryService<ArticleQueryCommand> queryService;

      public ArticleSearchDelegate()
      {
         ArticleSearchStrategy searchStrategy = new ArticleSearchStrategy();
         queryService = searchServiceMgr.getQueryService(searchStrategy);
      }

      public ArticleSearchResult search(String query, int offset, int numResults)
      {
         ArticleQueryCommand queryCommand = queryService.createQuery();
         queryCommand.setQuery(query);
         queryCommand.setOffset(offset);
         queryCommand.setMaxResults(numResults);
         return queryCommand.execute();
      }
   }
}
