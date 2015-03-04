package edu.tamu.tcat.trc.entries.bib.copy;

import java.util.Collection;

import edu.tamu.tcat.hathitrust.Record;

public interface DigitalContentSearchService
{

   /**
    * Retrieves all digital content from each service that is currently registered in the application.
    * Currently we are implementing HathiTrust, but others will soon follow.
    * @param query
    * @return
    */
   Collection<DigitalContentReference> searchForDigitalContent(String query);

   DigitalContentSearchCommand createQueryString();

   /**
    * Retrieves records from the specified provider
    * @param provider
    * @param recordNumber
    * @return A collection of Records that represent the record number provided
    */
   Collection<Record> getBibligraphicRecords(DigitalCopyProvider provider, String recordNumber);

   DigitalContentSearchCommand createRequest();

}
