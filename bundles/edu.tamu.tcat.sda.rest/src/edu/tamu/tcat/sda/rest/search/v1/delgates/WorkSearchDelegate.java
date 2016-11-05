package edu.tamu.tcat.sda.rest.search.v1.delgates;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.sda.rest.search.v1.SearchDelegate;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.BibliographicSearchStrategy;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.WorkSolrQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.BiblioRestApiV1;
import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.entries.types.biblio.search.SearchWorksResult;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;
import edu.tamu.tcat.trc.resolver.EntryIdDto;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.QueryService;

public class WorkSearchDelegate implements SearchDelegate<BibliographicEntry, BiblioRestApiV1.WorkSearchResult>
{
   public static final String DELEGATE_NAME = "works";

   private final TrcApplication trcCtx;
   private final BibliographicSearchStrategy strategy;

   public WorkSearchDelegate(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
      this.strategy = new BibliographicSearchStrategy(trcCtx);
   }

   @Override
   public String getName()
   {
      return DELEGATE_NAME;
   }

   @Override
   public IndexServiceStrategy<BibliographicEntry, ?> getSearchStrategy()
   {
      return strategy;
   }

   @Override
   public CompletableFuture<Collection<BiblioRestApiV1.WorkSearchResult>> search(String query, int offset, int numResults)
   {
      QueryService<WorkSolrQueryCommand> queryService = trcCtx.getQueryService(strategy);

      WorkQueryCommand queryCommand = queryService.createQuery();
      queryCommand.query(query);
      queryCommand.setOffset(offset);
      queryCommand.setMaxResults(numResults);

      return queryCommand.execute().thenApply(this::adapt);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Class<BibliographicEntryRepository> getRepoType()
   {
      return BibliographicEntryRepository.class;
   }

   private Collection<BiblioRestApiV1.WorkSearchResult> adapt(SearchWorksResult result)
   {
      return result.get().stream().map(this::adapt).collect(toList());
   }

   private BiblioRestApiV1.WorkSearchResult adapt(BiblioSearchProxy orig)
   {
      // HACK copy/paste code
      EntryResolverRegistry resolvers = trcCtx.getResolverRegistry();
      BiblioRestApiV1.WorkSearchResult dto = new BiblioRestApiV1.WorkSearchResult();
      dto.id = orig.id;
      dto.ref = orig.token == null ? null : EntryIdDto.adapt(resolvers.getReference(orig.token));
      dto.type = orig.type;
      dto.label = orig.label;
      dto.title = orig.title;
      dto.uri = orig.uri;
      dto.pubYear = orig.pubYear;
      dto.summary = orig.summary;
      if (orig.authors != null)
         dto.authors = orig.authors.stream().map(this::adapt).collect(toList());
      return dto;
   }

   private BiblioRestApiV1.AuthorRef adapt(BiblioSearchProxy.AuthorProxy author)
   {
      BiblioRestApiV1.AuthorRef dto = new BiblioRestApiV1.AuthorRef();
      dto.authorId = author.authorId;
      dto.firstName = author.firstName;
      dto.lastName = author.lastName;
      dto.role = author.role;

      return dto;
   }

}