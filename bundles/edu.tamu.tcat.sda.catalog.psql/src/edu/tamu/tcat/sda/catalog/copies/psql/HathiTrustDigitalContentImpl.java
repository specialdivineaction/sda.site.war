package edu.tamu.tcat.sda.catalog.copies.psql;

import edu.tamu.tcat.sda.catalog.copies.DigitalContentReference;
import edu.tamu.tcat.sda.catalog.copies.DigitalContentReferenceDTO;
import edu.tamu.tcat.sda.catalog.copies.DigitalCopyProvider;

public class HathiTrustDigitalContentImpl implements DigitalContentReference
{
   private DigitalCopyProvider provider;
   private String recordNumber;
   private String access;
   private String rights;
   private String source;
   private String sourceRecordNumber;
   private String title;

   public HathiTrustDigitalContentImpl(DigitalContentReferenceDTO copy)
   {
      this.provider = DigitalCopyProvider.HathiTrust;
      this.recordNumber = copy.recordNumber;
      this.access = copy.access;
      this.rights = copy.rights;
      this.source = copy.source;
      this.sourceRecordNumber = copy.sourceRecordNumber;
      this.title = copy.title;
   }


   @Override
   public DigitalCopyProvider getProvider()
   {
      return provider;
   }
   @Override
   public String getRecordNumber()
   {
      return this.recordNumber;
   }
   @Override
   public String getAccess()
   {
      return this.access;
   }
   @Override
   public String getRights()
   {
      return this.rights;
   }
   @Override
   public String getSource()
   {
      return this.source;
   }
   @Override
   public String getSourceRecordNumber()
   {
      return this.sourceRecordNumber;
   }
   @Override
   public String getTitle()
   {
      return this.title;
   }
}
