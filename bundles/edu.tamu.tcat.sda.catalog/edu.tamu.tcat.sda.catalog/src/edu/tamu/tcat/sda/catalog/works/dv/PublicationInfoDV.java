package edu.tamu.tcat.sda.catalog.works.dv;

import edu.tamu.tcat.sda.catalog.works.PublicationInfo;

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
