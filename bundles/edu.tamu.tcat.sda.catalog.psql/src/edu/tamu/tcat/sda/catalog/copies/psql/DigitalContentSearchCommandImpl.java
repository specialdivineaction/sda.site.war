package edu.tamu.tcat.sda.catalog.copies.psql;

import java.util.Collection;
import java.util.HashSet;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.Record.IdType;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.client.v1.basic.BasicRecord;
import edu.tamu.tcat.hathitrust.client.v1.basic.BasicRecord.BasicRecordIdentifier;
import edu.tamu.tcat.hathitrust.client.v1.basic.BibAPIClientImpl;
import edu.tamu.tcat.sda.catalog.copies.DigitalContentSearchCommand;

public class DigitalContentSearchCommandImpl implements DigitalContentSearchCommand
{


   public Collection<Record> getHathiTrustContent(String recordNumber)
   {
      Collection<Record> records = new HashSet<>();
      BasicRecord basicRecord = new BasicRecord();
      BasicRecordIdentifier id = basicRecord.new BasicRecordIdentifier(IdType.RECORDNUMBER, recordNumber);
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
