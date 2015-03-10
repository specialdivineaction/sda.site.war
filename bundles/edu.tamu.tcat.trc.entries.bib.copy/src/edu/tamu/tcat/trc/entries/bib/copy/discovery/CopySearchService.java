package edu.tamu.tcat.trc.entries.bib.copy.discovery;

import edu.tamu.tcat.trc.entries.bib.copy.ResourceAccessException;


/**
 * Allows clients to search across multiple data sources to identify potentially relevant
 * digital copies of a bibliographic item. The search service accepts simple key word queries
 * and also allows for date range and author filters. In general, the search implementation
 * will prioritize results based on the date range and author filters but will not strictly
 * remove non-matching entities since not all data sources provide this information.
 *
 * <p>
 * Search results return serializable instances of {@link DigitalCopyProxy}s. This objects are
 * intended to be lightweight identifiers that support basic display information so users can
 * readily identify copies that are of potential interest and uniquely identify those copies
 * in order to retrieve the full record corresponding to the proxy.
 *
 */
public interface CopySearchService
{
   /**
    * Attempts to find digital copies within the repository mediated this
    *
    * @param query
    * @return
    * @throws ResourceAccessException
    */
   CopySearchResult find(ContentQuery query) throws ResourceAccessException;
}
