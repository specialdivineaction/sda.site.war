package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.sda.catalog.works.DateDescription;
import edu.tamu.tcat.sda.catalog.works.PublicationInfo;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;

public class PublicationImpl implements PublicationInfo
{
   PublicationInfoDV pubInfo;
   
   public PublicationImpl(PublicationInfoDV publicationInfo)
   {
      this.pubInfo = publicationInfo;
   }
   
   @Override
   public String getLocation()
   {
      return pubInfo.place;
   }

   @Override
   public String getPublisher()
   {
      return pubInfo.publisher;
   }

   @Override
   public DateDescription getPublicationDate()
   {
      return new DateDescriptionImpl(pubInfo.date);
   }

}
