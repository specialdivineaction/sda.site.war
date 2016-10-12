package edu.tamu.tcat.sda.rest.search.v1;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1.ArticleSearchResult;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.RestApiV1.WorkSearchResult;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiV1.SimplePerson;

public abstract class RestApiV1
{
   public static class UnifiedResult
   {
      public String query;
      public int offset;
      public int max;
      public final List<WorkSearchResult> works = new ArrayList<>();
      public final List<SimplePerson> people = new ArrayList<>();
      public final List<ArticleSearchResult> articles = new ArrayList<>();
   }
}
