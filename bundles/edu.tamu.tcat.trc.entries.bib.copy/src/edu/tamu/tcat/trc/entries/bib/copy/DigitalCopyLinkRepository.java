package edu.tamu.tcat.trc.entries.bib.copy;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.sda.datastore.DataStore;
import edu.tamu.tcat.trc.entries.bib.copy.rest.v1.DigitalCopyLinkDTO;

public interface DigitalCopyLinkRepository extends DataStore
{
   void create(DigitalCopyLinkDTO dcl);

   /**
    * @param entity The URI of the bibliographic entity for which copies should be returned.
    *       Note that this may be a work, edition or volume. This method will return copies for
    *       the identified object and all component entities as well.
    * @return The
    */
   Set<CopyReference> getCopies(URI entity);

   CopyReference getCopyReference(UUID id);



   @Deprecated
   Iterable<DigitalCopyLink> getLinks();

   @Deprecated
   Iterable<DigitalCopyLink> getLinks(String bibliography);
}
