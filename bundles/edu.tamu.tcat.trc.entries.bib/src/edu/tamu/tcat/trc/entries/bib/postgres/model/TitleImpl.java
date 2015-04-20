package edu.tamu.tcat.trc.entries.bib.postgres.model;

import java.util.Objects;

import edu.tamu.tcat.trc.entries.bib.Title;
import edu.tamu.tcat.trc.entries.bib.dto.TitleDV;

public class TitleImpl implements Title
{
   private final String title;
   private final String subTitle;
   private final String type;
   private final String language;

   public TitleImpl(TitleDV titleDV)
   {
      title = titleDV.title;
      subTitle = titleDV.subtitle;
      type = titleDV.type;
      language = titleDV.lg;
   }

   @Override
   public String getTitle()
   {
      return this.title;
   }

   @Override
   public String getSubTitle()
   {
      return this.subTitle;
   }

   @Override
   public String getFullTitle()
   {
      StringBuilder sb = new StringBuilder();

      sb.append(this.title);

      if (this.subTitle != null && !this.subTitle.trim().isEmpty()) {
         sb.append(": ").append(this.subTitle);
      }

      return sb.toString();
   }

   @Override
   public String getType()
   {
      return this.type;
   }

   @Override
   public String getLanguage()
   {
      return this.language;
   }

   @Override
   public String toString()
   {
      return getFullTitle();
   }

   @Override
   public int hashCode()
   {
      int result = 17;

      result = 37 * result + (title == null ? 0 : title.hashCode());
      result = 37 * result + (subTitle == null ? 0 : subTitle.hashCode());
      result = 37 * result + (type == null ? 0 : type.hashCode());
      result = 37 * result + (language == null ? 0 : language.hashCode());

      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof Title)) {
         return false;
      }

      Title t = (Title)obj;

      return Objects.equals(t.getTitle(), title) &&
            Objects.equals(t.getSubTitle(), subTitle) &&
            Objects.equals(t.getType(), type) &&
            Objects.equals(t.getLanguage(), language);
   }

}
