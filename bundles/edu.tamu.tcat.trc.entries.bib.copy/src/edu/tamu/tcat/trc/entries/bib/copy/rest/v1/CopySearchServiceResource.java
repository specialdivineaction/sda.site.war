package edu.tamu.tcat.trc.entries.bib.copy.rest.v1;

import java.time.temporal.TemporalAccessor;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import edu.tamu.tcat.trc.entries.bib.copy.ResourceAccessException;
import edu.tamu.tcat.trc.entries.bib.copy.discovery.ContentQuery;
import edu.tamu.tcat.trc.entries.bib.copy.discovery.CopySearchResult;
import edu.tamu.tcat.trc.entries.bib.copy.discovery.CopySearchService;
import edu.tamu.tcat.trc.entries.bib.copy.hathitrust.HTFilesSearchService;


@Path("/copies/search")
public class CopySearchServiceResource
{
   private static final Logger logger = Logger.getLogger(CopySearchServiceResource.class.getName());
   private CopySearchService searchService;
   private static final String DEFAULT_DATE = String.valueOf(Integer.MIN_VALUE);

   // called by DS
   public void setRepository(CopySearchService svc)
   {
      this.searchService = svc;
   }

   // called by DS
   public void activate()
   {
      if (this.searchService == null)
      {
         // HACK: auto generate HathiTrust search service
         this.searchService = new HTFilesSearchService();
      }
   }

   // called by DS
   public void dispose()
   {
   }


   public SearchResult search(@PathParam(value = "q") String q,
                              @PathParam(value = "author") String author,
                              @DefaultValue("-9999") @PathParam(value = "before") int before,
                              @DefaultValue("-9999") @PathParam(value = "after") int after,
                              @DefaultValue("0") @PathParam(value = "offset") int offset,
                              @DefaultValue("25") @PathParam(value = "limit") int limit
                              )
   {
//      CopySearchService htSearch = new HTFilesSearchService();
      try
      {
         CopyQueryImpl query = new CopyQueryImpl(q, author, before, after, offset, limit);
         CopySearchResult result = searchService.find(query);
         CopyQueryDTO copyDTO = new CopyQueryDTO(query);
         return new SearchResult(result, copyDTO );
      }
      catch (ResourceAccessException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      throw new UnsupportedOperationException();
   }


   private static class CopyQueryImpl implements ContentQuery
   {
      public String q;
      public String author;
      public int before;
      public int after;
      public int offset;
      public int limit;

      public CopyQueryImpl(String keyWords, String author, int before, int after, int offset, int limit)
      {
         this.q = keyWords;
         this.author = author;
         this.before = before;
         this.after = after;
         this.offset = offset;
         this.limit = limit;
      }

      @Override
      public String getKeyWordQuery()
      {
         return q;
      }

      @Override
      public String getAuthorQuery()
      {
         return author;
      }

      @Override
      public TemporalAccessor getDateRangeStart()
      {
         if (before != -9999)
            return  java.time.Year.of(before);
         else
            return null;
      }

      @Override
      public TemporalAccessor getDateRangeEnd()
      {
         if (after != -9999)
            return  java.time.Year.of(after);
         else
            return null;
      }

      @Override
      public int getOffset()
      {
         return offset;
      }

      @Override
      public int getLimit()
      {
         return limit;
      }
   }
}
