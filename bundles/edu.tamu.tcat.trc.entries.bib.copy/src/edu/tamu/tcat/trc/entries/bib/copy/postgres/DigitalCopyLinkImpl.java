package edu.tamu.tcat.trc.entries.bib.copy.postgres;

import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopyLink;
import edu.tamu.tcat.trc.entries.bib.copy.rest.v1.DigitalCopyLinkDTO;

public class DigitalCopyLinkImpl implements DigitalCopyLink
{

   private final String linkUrl;
   private final String origin;
   private final String rightsCode;
   private final String bibliography;

   public DigitalCopyLinkImpl(DigitalCopyLinkDTO dcl)
   {
      this.linkUrl = dcl.linkUrl;
      this.origin = dcl.origin;
      this.rightsCode = dcl.rightsCode;
      this.bibliography = dcl.bibliography;
   }

   @Override
   public String getLinkUrl()
   {
      return this.linkUrl;
   }

   @Override
   public String getOrigin()
   {
      return this.origin;
   }

   @Override
   public String getRightsCode()
   {
      return this.rightsCode;
   }

   @Override
   public String getBibliography()
   {
      return this.bibliography;
   }
}
