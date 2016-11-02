package edu.tamu.tcat.sda.rest.search.v1;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.RestApiV1.WorkSearchResult;
import edu.tamu.tcat.trc.entries.types.biblio.search.SearchWorksResult;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiV1.SimplePerson;
import edu.tamu.tcat.trc.entries.types.bio.search.PersonSearchResult;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public abstract class SearchAdapter
{
   public static List<WorkSearchResult> adapt(SearchWorksResult model, EntryResolverRegistry resolvers)
   {
      return edu.tamu.tcat.trc.entries.types.biblio.rest.v1.SearchAdapter.toDTO(model.get(), resolvers);
   }

   public static List<SimplePerson> adapt(PersonSearchResult model, EntryResolverRegistry resolvers)
   {
      return edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.SearchAdapter.toDTO(model.get(), resolvers);
   }

   public static List<edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1.ArticleSearchResult> adapt(ArticleSearchResult model, EntryResolverRegistry resolvers)
   {
      return edu.tamu.tcat.trc.entries.types.article.rest.v1.ModelAdapter.toDTO(model, resolvers);
   }
}
