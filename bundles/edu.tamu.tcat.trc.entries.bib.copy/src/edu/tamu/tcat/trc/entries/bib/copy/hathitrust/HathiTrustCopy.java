package edu.tamu.tcat.trc.entries.bib.copy.hathitrust;

import java.net.URI;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import edu.tamu.tcat.hathitrust.Item;
import edu.tamu.tcat.hathitrust.MarcRecord;
import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.Record.IdType;
import edu.tamu.tcat.hathitrust.Record.RecordIdentifier;
import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopy;

/**
 *  Represents a digital copy of a book from the HathiTrust digital library.
 *  TODO document where to look for the supported data formats
 */
public class HathiTrustCopy implements DigitalCopy
{

   private String id;
   private List<String> titles;
   private List<TemporalAccessor> publishDates;
   private List<Item> items;
   private MarcRecord marcRecord;
   private URI recordURL;
   private List<RecordIdentifier> isbns;
   private List<RecordIdentifier> issns;
   private List<RecordIdentifier> lccns;
   private List<RecordIdentifier> oclcs;

   public HathiTrustCopy()
   {

   }

   public HathiTrustCopy(Record record)
   {
      id = record.getId();
      titles = record.getTitles();
      publishDates = record.getPublishDates();
      items = record.getItems();
      marcRecord = record.getMarcRecord();
      recordURL = record.getRecordURL();

      isbns = record.getIdentifiers(IdType.ISBN);
      issns = record.getIdentifiers(IdType.ISSN);
      lccns = record.getIdentifiers(IdType.LCCN);
      oclcs = record.getIdentifiers(IdType.OCLC);
   }

   public String getRecordId()
   {
      return this.id;
   }

   public List<String> getTitles()
   {
      return this.titles;
   }

   public List<TemporalAccessor> getPublishDates()
   {
      return this.publishDates;
   }

   public List<Item> getItems()
   {
      return this.items;
   }

   public MarcRecord getMarc()
   {
      return this.marcRecord;
   }

   public URI getRecordURL()
   {
      return this.recordURL;
   }

   public List<RecordIdentifier> getISBNs()
   {
      return this.isbns;
   }

   public List<RecordIdentifier> getISSNs()
   {
      return this.issns;
   }

   public List<RecordIdentifier> getLCCNs()
   {
      return this.lccns;
   }

   public List<RecordIdentifier> getOCLCs()
   {
      return this.oclcs;
   }

}
