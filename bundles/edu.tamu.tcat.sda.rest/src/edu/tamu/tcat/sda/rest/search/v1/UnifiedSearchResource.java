package edu.tamu.tcat.sda.rest.search.v1;

import static java.text.MessageFormat.format;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.sda.rest.search.v1.delgates.ArticleSearchDelegate;
import edu.tamu.tcat.sda.rest.search.v1.delgates.PeopleSearchDelegate;
import edu.tamu.tcat.sda.rest.search.v1.delgates.RelationshipSearchDelegate;
import edu.tamu.tcat.sda.rest.search.v1.delgates.WorkSearchDelegate;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.impl.psql.entries.SolrSearchSupport;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.services.rest.ApiUtils;

public class UnifiedSearchResource
{
   static final Logger logger = Logger.getLogger(PeopleSearchDelegate.class.getName());

   private final Map<String, SearchDelegate<?, ?>> delegates = new HashMap<>();

   private final TrcApplication trcCtx;

   public UnifiedSearchResource(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;

      this.delegates.put(WorkSearchDelegate.DELEGATE_NAME, new WorkSearchDelegate(trcCtx));
      this.delegates.put(PeopleSearchDelegate.DELEGATE_NAME, new PeopleSearchDelegate(trcCtx));
      this.delegates.put(ArticleSearchDelegate.DELEGATE_NAME, new ArticleSearchDelegate(trcCtx));
      this.delegates.put(RelationshipSearchDelegate.DELEGATE_NAME, new RelationshipSearchDelegate(trcCtx));
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.UnifiedResult search(@QueryParam("q") String query,
                                         @QueryParam(value = "off") @DefaultValue("0")   int offset,
                                         @QueryParam(value = "max") @DefaultValue("100") int numResults)
   {
      RestApiV1.UnifiedResult dto = new RestApiV1.UnifiedResult();
      dto.query = query;
      dto.offset = offset;
      dto.max = numResults;

      delegates.keySet().parallelStream().forEach(key -> {
         SearchDelegate<?, ?> delegate = delegates.get(key);
         CompletableFuture<?> result = delegate.search(query, offset, numResults);
         try
         {
            Object obj = result.get(10, TimeUnit.SECONDS);
            dto.results.put(key, obj);
         }
         catch (Exception ex)
         {
            logger.log(Level.SEVERE, format("Delegated search failed for {0}", key), ex);
            dto.results.put(key, Collections.emptyList());
         }
      });

      return dto;
   }

   @GET
   @Path("{delegateKey}")
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.UnifiedResult search(@PathParam("delegateKey") String key,
                                         @QueryParam("q") String query,
                                         @QueryParam(value = "off") @DefaultValue("0")   int offset,
                                         @QueryParam(value = "max") @DefaultValue("100") int numResults)
   {
      RestApiV1.UnifiedResult dto = new RestApiV1.UnifiedResult();
      dto.query = query;
      dto.offset = offset;
      dto.max = numResults;

      String notFountMsg = "Search support has not been configured for {0}";
      if (!delegates.containsKey(key))
         throw ApiUtils.raise(Response.Status.NOT_FOUND, format(notFountMsg, key), Level.WARNING, null);

      CompletableFuture<?> results = delegates.get(key).search(query, offset, numResults);
      try
      {
         dto.results.put(key, results.get(10, TimeUnit.SECONDS));
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, format("Delegated search failed for {0}", key), ex);
         dto.results.put(key, Collections.emptyList());
      }

      return dto;
   }


   @POST
   @Path("reindex/{delegateKey}")
   public void reindexWorks(@PathParam("delegateKey") String key)
   {
      String notFountMsg = "Search support has not been configured for {0}";
      if (!delegates.containsKey(key))
         throw ApiUtils.raise(Response.Status.NOT_FOUND, format(notFountMsg, key), Level.WARNING, null);

      reindex(delegates.get(key));
   }

   public <T> void reindex(SearchDelegate<T, ?> delegate)
   {
      IndexServiceStrategy<T, ?> searchStrategy = delegate.getSearchStrategy();

      Class<T> entryClass = searchStrategy.getType();
      IndexService<T> indexService = trcCtx.getIndexService(entryClass);
      SolrSearchSupport<T> solrIndex = new SolrSearchSupport<>(indexService, searchStrategy);

      EntryRepository<T> repository = trcCtx.getRepository(null, delegate.getRepoType());
      solrIndex.reIndex(repository);
   }
}
