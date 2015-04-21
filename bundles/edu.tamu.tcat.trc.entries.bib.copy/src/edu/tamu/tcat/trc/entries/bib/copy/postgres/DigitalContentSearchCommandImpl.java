package edu.tamu.tcat.trc.entries.bib.copy.postgres;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.hathitrust.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.bibliography.BasicRecordIdentifier;
import edu.tamu.tcat.hathitrust.bibliography.Record;
import edu.tamu.tcat.hathitrust.bibliography.Record.IdType;
import edu.tamu.tcat.hathitrust.client.v1.basic.BibAPIClientImpl;
import edu.tamu.tcat.trc.entries.bib.copy.legacy.DigitalContentSearchCommand;

public class DigitalContentSearchCommandImpl implements DigitalContentSearchCommand
{
   private final Logger logger = Logger.getLogger("edu.tamu.tcat.trc.entries.bib.copy.postgres");

   public Collection<Record> getHathiTrustContent(String recordNumber)
   {
      Collection<Record> records = new HashSet<>();
      BasicRecordIdentifier id = new BasicRecordIdentifier(IdType.RECORDNUMBER, recordNumber);
      BibAPIClientImpl client = new BibAPIClientImpl();
      try
      {
         records = client.lookup(id);
      }
      catch (HathiTrustClientException e)
      {
         logger.log(Level.FINE, "An error occured while retrieving records from HathiTrust, record[" + recordNumber + "]");
      }
      return records;
   }

}
