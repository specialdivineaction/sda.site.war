package edu.tamu.tcat.sda.rest.search.v1;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.impl.search.ArticleSearchStrategy;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.BibliographicSearchStrategy;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.WorkSolrQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.search.SearchWorksResult;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.BioSearchStrategy;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.search.BioEntryQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.search.PersonSearchResult;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.impl.search.RelnSearchStrategy;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchResult;
import edu.tamu.tcat.trc.impl.psql.entries.SolrSearchSupport;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

public class UnifiedSearchResource
{
   private static final Logger logger = Logger.getLogger(UnifiedSearchResource.PeopleSearchDelegate.class.getName());

   private final SearchServiceManager searchServiceMgr;
   private final EntryRepositoryRegistry repoRegistry;
   private final ConfigurationProperties config;

   private final WorkSearchDelegate workSearchDelegate;
   private final PeopleSearchDelegate peopleSearchDelegate;
   private final ArticleSearchDelegate articleSearchDelegate;
   private final RelationshipSearchDelegate relnSearchDelegate;

   public UnifiedSearchResource(SearchServiceManager searchServiceMgr, EntryRepositoryRegistry repoRegistry, ConfigurationProperties config)
   {
      this.searchServiceMgr = searchServiceMgr;
      this.repoRegistry = repoRegistry;
      this.config = config;

      this.workSearchDelegate = new WorkSearchDelegate();
      this.peopleSearchDelegate = new PeopleSearchDelegate();
      this.articleSearchDelegate = new ArticleSearchDelegate();
      this.relnSearchDelegate = new RelationshipSearchDelegate();
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

   @POST
   @Path("reindex/works")
   public void reindexWorks()
   {
      BibliographicEntryRepository repository = repoRegistry.getRepository(null, BibliographicEntryRepository.class);
      reindex(workSearchDelegate, repository);
   }

   @POST
   @Path("reindex/people")
   public void reindexPeople()
   {
      BiographicalEntryRepository repository = repoRegistry.getRepository(null, BiographicalEntryRepository.class);
      reindex(peopleSearchDelegate, repository);
   }

   @POST
   @Path("reindex/articles")
   public void reindexArticles()
   {
      ArticleRepository repository = repoRegistry.getRepository(null, ArticleRepository.class);
      reindex(articleSearchDelegate, repository);
   }

   @POST
   @Path("reindex/relationships")
   public void reindexRelationships()
   {
      RelationshipRepository repository = repoRegistry.getRepository(null, RelationshipRepository.class);
      reindex(relnSearchDelegate, repository);
   }

   private <T> void reindex(SearchDelegate<T, ?> delegate, EntryRepository<T> repository)
   {
      IndexServiceStrategy<T, ?> searchStrategy = delegate.getSearchStrategy();

      Class<T> entryClass = searchStrategy.getType();
      IndexService<T> indexService = searchServiceMgr.getIndexService(entryClass);
      SolrSearchSupport<T> solrIndex = new SolrSearchSupport<>(indexService, searchStrategy);
      solrIndex.reIndex(repository);
   }

   private static interface SearchDelegate<EntryType, ResultType>
   {
      IndexServiceStrategy<EntryType, ?> getSearchStrategy();

      ResultType search(String query, int offset, int numResults);
   }

   private class WorkSearchDelegate implements SearchDelegate<BibliographicEntry, SearchWorksResult>
   {
      private final BibliographicSearchStrategy searchStrategy = new BibliographicSearchStrategy();

      @Override
      public IndexServiceStrategy<BibliographicEntry, ?> getSearchStrategy()
      {
         return searchStrategy;
      }

      @Override
      public SearchWorksResult search(String query, int offset, int numResults)
      {
         QueryService<WorkSolrQueryCommand> queryService = searchServiceMgr.getQueryService(searchStrategy);

         WorkQueryCommand queryCommand = queryService.createQuery();
         queryCommand.query(query);
         queryCommand.setOffset(offset);
         queryCommand.setMaxResults(numResults);
         return queryCommand.execute();
      }
   }

   private class PeopleSearchDelegate implements SearchDelegate<BiographicalEntry, PersonSearchResult>
   {
      private final BioSearchStrategy searchStrategy;

      public PeopleSearchDelegate()
      {
         searchStrategy = new BioSearchStrategy(config);
      }

      @Override
      public IndexServiceStrategy<BiographicalEntry, ?> getSearchStrategy()
      {
         return searchStrategy;
      }

      @Override
      public PersonSearchResult search(String query, int offset, int numResults)
      {
         QueryService<BioEntryQueryCommand> queryService = searchServiceMgr.getQueryService(searchStrategy);

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

   private class ArticleSearchDelegate implements SearchDelegate<Article, ArticleSearchResult>
   {
      private final ArticleSearchStrategy searchStrategy = new ArticleSearchStrategy();

      @Override
      public IndexServiceStrategy<Article, ?> getSearchStrategy()
      {
         return searchStrategy;
      }

      @Override
      public ArticleSearchResult search(String query, int offset, int numResults)
      {
         QueryService<ArticleQueryCommand> queryService = searchServiceMgr.getQueryService(searchStrategy);

         ArticleQueryCommand queryCommand = queryService.createQuery();
         queryCommand.setQuery(query);
         queryCommand.setOffset(offset);
         queryCommand.setMaxResults(numResults);
         return queryCommand.execute();
      }
   }

   private class RelationshipSearchDelegate implements SearchDelegate<Relationship, RelationshipSearchResult>
   {
      EntryResolverRegistry resolvers = repoRegistry.getResolverRegistry();
      private final RelnSearchStrategy searchStrategy = new RelnSearchStrategy(resolvers);

      @Override
      public IndexServiceStrategy<Relationship, ?> getSearchStrategy()
      {
         return searchStrategy;
      }

      @Override
      public RelationshipSearchResult search(String query, int offset, int numResults)
      {
         // TODO Auto-generated method stub
         throw new UnsupportedOperationException("not implemented");
      }
   }
}
