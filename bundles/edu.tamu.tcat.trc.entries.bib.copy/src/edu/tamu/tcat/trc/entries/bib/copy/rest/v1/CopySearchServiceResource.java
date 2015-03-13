package edu.tamu.tcat.trc.entries.bib.copy.rest.v1;

import java.time.temporal.TemporalAccessor;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import edu.tamu.tcat.trc.entries.bib.copy.discovery.ContentQuery;
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
                              @DefaultValue("-9999") @PathParam(value = "before") int after,
                              @DefaultValue("0") @PathParam(value = "before") int offset
                              @DefaultValue("25") @PathParam(value = "before") int limit
                              )
   {

      throw new UnsupportedOperationException();
   }


   private static class CopyQueryImpl implements ContentQuery
   {

      public CopyQueryImpl()
      {

      }

      @Override
      public String getKeyWordQuery()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public String getAuthorQuery()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public TemporalAccessor getDateRangeStart()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public TemporalAccessor getDateRangeEnd()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public int getOffset()
      {
         return 0;
      }

      @Override
      public int getLimit()
      {
         return 20;
      }
   }
}
