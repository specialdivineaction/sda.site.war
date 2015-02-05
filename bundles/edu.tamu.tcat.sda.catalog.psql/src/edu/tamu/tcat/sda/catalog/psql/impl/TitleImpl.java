package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;

public class TitleImpl implements Title
{
   private final TitleDV title;

   public TitleImpl(TitleDV titleDV)
   {
      this.title = titleDV;
   }
   @Override
   public String getTitle()
   {
      return title.title;
   }

   @Override
   public String getSubTitle()
   {
      return title.subtitle;
   }

   @Override
   public String getFullTitle()
   {
      StringBuilder sb = new StringBuilder();

      sb.append(title.title);

      if (title.subtitle != null && !title.subtitle.trim().isEmpty()) {
         sb.append(": ").append(title.subtitle);
      }

      return sb.toString();
   }
   @Override
   public String getType()
   {
      return title.type;
   }
   @Override
   public String getLanuguage()
   {
      return title.lg;
   }

}
