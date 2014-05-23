package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.Date;

import edu.tamu.tcat.sda.catalog.works.PublicationInfo;
import edu.tamu.tcat.sda.catalog.works.dv.DateDescriptionDV;
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
   public Date getPublicationDate()
   {
      DateDescriptionDV dateDescript = pubInfo.date;
      return dateDescript.value;
   }

}
