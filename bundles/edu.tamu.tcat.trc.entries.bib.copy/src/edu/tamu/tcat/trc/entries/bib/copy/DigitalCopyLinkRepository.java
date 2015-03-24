package edu.tamu.tcat.trc.entries.bib.copy;

import edu.tamu.tcat.sda.datastore.DataStore;
import edu.tamu.tcat.trc.entries.bib.copy.rest.v1.DigitalCopyLinkDTO;

public interface DigitalCopyLinkRepository extends DataStore
{
   void create(DigitalCopyLinkDTO dcl);

   Iterable<DigitalCopyLink> getLinks();

   Iterable<DigitalCopyLink> getLinks(String bibliography);
}
