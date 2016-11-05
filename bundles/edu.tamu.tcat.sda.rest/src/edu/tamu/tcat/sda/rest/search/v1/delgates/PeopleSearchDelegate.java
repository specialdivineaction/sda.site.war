package edu.tamu.tcat.sda.rest.search.v1.delgates;

import static java.text.MessageFormat.format;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.ws.rs.core.Response;

import edu.tamu.tcat.sda.rest.search.v1.SearchDelegate;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.BioSearchStrategy;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiAdapter;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiV1;
import edu.tamu.tcat.trc.entries.types.bio.search.BioEntryQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.search.PersonSearchResult;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.services.rest.ApiUtils;

public class PeopleSearchDelegate implements SearchDelegate<BiographicalEntry, RestApiV1.BasicBioEntry>
{
   public static final String DELEGATE_NAME = "people";

   private final BioSearchStrategy searchStrategy;
   private final TrcApplication trcCtx;

   public PeopleSearchDelegate(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
      this.searchStrategy = new BioSearchStrategy(trcCtx);
   }

   @Override
   public String getName()
   {
      return DELEGATE_NAME;
   }

   @Override
   public IndexServiceStrategy<BiographicalEntry, ?> getSearchStrategy()
   {
      return searchStrategy;
   }

   @Override
   public CompletableFuture<Collection<RestApiV1.BasicBioEntry>> search(String query, int offset, int numResults)
   {
      try
      {
         QueryService<BioEntryQueryCommand> queryService = trcCtx.getQueryService(searchStrategy);

         BioEntryQueryCommand queryCommand = queryService.createQuery();
         queryCommand.query(query);
         queryCommand.setOffset(offset);
         queryCommand.setMaxResults(numResults);

         CompletableFuture<PersonSearchResult> future = queryCommand.execute();

         return future.thenApply(this::adapt);
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
   public Class<BiographicalEntryRepository> getRepoType()
   {
      return BiographicalEntryRepository.class;
   }

   private Collection<RestApiV1.BasicBioEntry> adapt(PersonSearchResult result)
   {
      return RestApiAdapter.adapt(result.get(), trcCtx.getResolverRegistry());
   }
}