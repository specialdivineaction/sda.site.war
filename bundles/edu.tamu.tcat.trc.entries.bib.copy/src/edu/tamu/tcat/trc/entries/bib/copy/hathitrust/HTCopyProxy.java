package edu.tamu.tcat.trc.entries.bib.copy.hathitrust;

import edu.tamu.tcat.trc.entries.bib.copy.discovery.DigitalCopyProxy;

public class HTCopyProxy implements DigitalCopyProxy
{
   public String ident;
   public String title;
   public String description;
   public String copyProvider = "HathiTrust";
   public String sourceSummary;
   public String rights;
   public String publicationDate;

   public HTCopyProxy()
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public String getIdentifier()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getTitle()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getCopyProvider()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getSourceSummary()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getRights()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getPublicationDate()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
