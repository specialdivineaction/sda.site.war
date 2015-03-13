package edu.tamu.tcat.trc.entries.bib.copy.rest.v1;

import java.util.Collection;

import edu.tamu.tcat.trc.entries.bib.copy.discovery.CopySearchResult;
import edu.tamu.tcat.trc.entries.bib.copy.discovery.DigitalCopyProxy;

public class SearchResult
{
   // Return q with proxy
   // { q: { },
   //   resutls: [ { these are the DigitalCopyProxy's} }
   public CopyQueryDTO query;
   public Collection<DigitalCopyProxy> copies;

   public SearchResult(CopySearchResult result, CopyQueryDTO query)
   {
      this.query = query;
      this.copies = result.asCollection();
   }

}
