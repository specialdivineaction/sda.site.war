package edu.tamu.tcat.catalogentries.works.dv;

import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.catalogentries.works.PublicationInfo;

public class PublicationInfoDV
{
   public String publisher;
   public String place;
   public DateDescriptionDV date;

   public PublicationInfoDV()
   {
   }

   public PublicationInfoDV(PublicationInfo pubInfo)
   {
      this.publisher = pubInfo.getPublisher();
      this.place = pubInfo.getLocation();
      this.date = new DateDescriptionDV(pubInfo.getPublicationDate());
   }
}
