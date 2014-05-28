package edu.tamu.tcat.sda.catalog.works.dv;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;

public class TitleDefinitionDV
{
   public TitleDV canonicalTitle;
   public TitleDV shortTitle;
   public Set<TitleDV> alternateTitles;
   public TitleDV localeTitle;
   
   public TitleDefinitionDV(TitleDefinition titleDef)
   {
     
      Set<TitleDV> titles = new HashSet<TitleDV>();
      Set<Title> altTitles = titleDef.getAlternateTitles();
      
      for(Title title : altTitles)
      {
         titles.add(new TitleDV(title));
      }
      
      this.canonicalTitle = new TitleDV(titleDef.getCanonicalTitle());
      this.shortTitle = new TitleDV(titleDef.getShortTitle());
      this.alternateTitles = titles;
      this.localeTitle = new TitleDV(titleDef.getTitle(Locale.US));
   }
   
   public TitleDefinitionDV()
   {
   }
}
