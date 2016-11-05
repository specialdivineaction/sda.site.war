package edu.tamu.tcat.sda.rest.search.v1.delgates;

import static java.text.MessageFormat.format;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.ws.rs.core.Response;

import edu.tamu.tcat.sda.rest.search.v1.SearchDelegate;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.impl.search.ArticleSearchStrategy;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1Adapter;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.services.rest.ApiUtils;

public class ArticleSearchDelegate implements SearchDelegate<Article, RestApiV1.ArticleSearchResult>
{
   public static final String DELEGATE_NAME = "articles";

   private final ArticleSearchStrategy strategy;
   private final TrcApplication trcCtx;

   public ArticleSearchDelegate(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
      this.strategy = new ArticleSearchStrategy(trcCtx.getResolverRegistry());
   }

   @Override
   public String getName()
   {
      return DELEGATE_NAME;
   }

   @Override
   public IndexServiceStrategy<Article, ?> getSearchStrategy()
   {
      return strategy;
   }

   @Override
   public CompletableFuture<Collection<RestApiV1.ArticleSearchResult>> search(String query, int offset, int numResults)
   {
      try
      {
         QueryService<ArticleQueryCommand> queryService = trcCtx.getQueryService(strategy);

         ArticleQueryCommand cmd = queryService.createQuery();
         cmd.query(query);
         cmd.setOffset(offset);
         cmd.setMaxResults(numResults);

         CompletableFuture<ArticleSearchResult> future = cmd.execute();

         RestApiV1Adapter adapter = new RestApiV1Adapter(trcCtx);
         return future.thenApply(adapter::toDTO);
      }
      catch (Exception e)
      {
         Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
         if (Error.class.isInstance(cause))
            throw (Error)cause;

         String msg = "Failed to get search results for people with query {0}";
         throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, format(msg, query), Level.SEVERE, (Exception)cause);
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public Class<ArticleRepository> getRepoType()
   {
      return ArticleRepository.class;
   }

}
