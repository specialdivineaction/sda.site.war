package edu.tamu.tcat.trc.entries.bib.copy.rest.v1;

import java.time.temporal.TemporalAccessor;

import edu.tamu.tcat.trc.resources.books.discovery.ContentQuery;

public class CopyQueryDTO
{
      public String q;
      public String author;
      public TemporalAccessor before;
      public TemporalAccessor after;
      public int offset;
      public int limit;

      public CopyQueryDTO(ContentQuery copyImpl)
      {
         this.q = copyImpl.getKeyWordQuery();
         this.author = copyImpl.getAuthorQuery();
         this.before = copyImpl.getDateRangeStart();
         this.after = copyImpl.getDateRangeEnd();
         this.offset = copyImpl.getOffset();
         this.limit = copyImpl.getLimit();
      }

}
