package edu.tamu.tcat.trc.entries.bib.copy.postgres;

import java.util.Collection;
import java.util.HashSet;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.Record.IdType;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.client.v1.basic.BasicRecord.BasicRecordIdentifier;
import edu.tamu.tcat.hathitrust.client.v1.basic.BibAPIClientImpl;
import edu.tamu.tcat.trc.entries.bib.copy.legacy.DigitalContentSearchCommand;

public class DigitalContentSearchCommandImpl implements DigitalContentSearchCommand
{


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
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return records;
   }

}
