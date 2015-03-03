package edu.tamu.tcat.catalogentries.bibliography.dv;

import edu.tamu.tcat.catalogentries.bibliography.Title;

public class TitleDV
{
   public String type;   // short, default, undefined.
   public String lg;
   public String title;
   public String subtitle;

   public TitleDV()
   {
   }

   public TitleDV(Title title)
   {
      this.type = title.getType();
      this.lg = title.getLanuguage();
      this.title = title.getTitle();
      this.subtitle = title.getSubTitle();
   }

}
