package edu.tamu.tcat.sda.rest.search.v1;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;

public interface SearchDelegate<EntryType, ResultType>
{
   String getName();

   IndexServiceStrategy<EntryType, ?> getSearchStrategy();

   CompletableFuture<Collection<ResultType>> search(String query, int offset, int numResults);

   <X extends EntryRepository<EntryType>> Class<X> getRepoType();
}