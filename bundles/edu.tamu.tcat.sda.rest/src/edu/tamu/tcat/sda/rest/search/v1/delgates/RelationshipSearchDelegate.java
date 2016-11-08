package edu.tamu.tcat.sda.rest.search.v1.delgates;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.sda.rest.search.v1.SearchDelegate;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.impl.search.RelnSearchStrategy;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.rest.v1.RestApiV1;
import edu.tamu.tcat.trc.entries.types.reln.rest.v1.SearchAdapter;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipDirection;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchResult;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.QueryService;

public class RelationshipSearchDelegate implements SearchDelegate<Relationship, RestApiV1.Relationship>
{
   public static final String DELEGATE_NAME = "relationships";

   private final RelnSearchStrategy strategy;
   private final TrcApplication trcCtx;

   public RelationshipSearchDelegate(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
      this.strategy = new RelnSearchStrategy(trcCtx);
   }

   @Override
   public String getName()
   {
      return DELEGATE_NAME;
   }

   @Override
   public IndexServiceStrategy<Relationship, ?> getSearchStrategy()
   {
      return strategy;
   }

   @Override
   public CompletableFuture<Collection<RestApiV1.Relationship>> search(String query, int offset, int numResults)
   {
      QueryService<RelationshipQueryCommand> queryService = trcCtx.getQueryService(strategy);

      RelationshipQueryCommand queryCommand = queryService.createQuery();

      EntryId entryId = trcCtx.getResolverRegistry().decodeToken(query);
      queryCommand.query(entryId, RelationshipDirection.any);
      queryCommand.setOffset(offset);
      queryCommand.setMaxResults(numResults);

      return queryCommand.execute().thenApply(this::adapt);
   }

   public List<RestApiV1.Relationship> adapt(RelationshipSearchResult results)
   {
      return SearchAdapter.toDTO(results.get(), trcCtx.getResolverRegistry(), null);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Class<RelationshipRepository> getRepoType()
   {
      return RelationshipRepository.class;
   }

}
