package edu.tamu.tcat.trc.entries.bib.copy.rest.v1;

import edu.tamu.tcat.trc.entries.bib.copy.postgres.DigitalCopyLinkImpl;

@Deprecated // Use CopyRefDTO based API
public class DigitalCopyLinkDTO
{

   public String linkUrl;
   public String origin;
   public String rightsCode;
   public String bibliography;

   public DigitalCopyLinkDTO()
   {
   }

   public DigitalCopyLinkDTO(DigitalCopyLinkImpl dcl)
   {
      this.linkUrl = dcl.getLinkUrl();
      this.origin = dcl.getOrigin();
      this.rightsCode = dcl.getRightsCode();
      this.bibliography = dcl.getBibliography();
   }

}
