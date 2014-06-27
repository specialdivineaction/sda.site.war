package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;


public class TitleDefinitionImpl implements TitleDefinition
{

   private TitleDV canonicalTitle;
   private TitleDV shortTitle;
   private TitleDV localeTitle;
   private Set<TitleDV> alternateTitles;


   public TitleDefinitionImpl(Set<TitleDV> titles)
   {
      alternateTitles = new HashSet<>();

      for (TitleDV title : titles)
      {
         String titleType = title.type;
         switch(titleType)
         {
            case "canonical":
               this.canonicalTitle = title;
               break;
            case "short":
               this.shortTitle = title;
               break;
            case "locale":
               this.localeTitle = title;
               break;
            case "alt":
               this.alternateTitles.add(title);
               break;
         }

      }

   }

   @Override
   public Title getCanonicalTitle()
   {
      return new TitleImpl(canonicalTitle);
   }

   @Override
   public Title getShortTitle()
   {
      return new TitleImpl(shortTitle);
   }

   @Override
   public Set<Title> getAlternateTitles()
   {
      Set<Title> titles = new HashSet<Title>();

      for(TitleDV title : alternateTitles)
      {
         titles.add(new TitleImpl(title));
      }

      return titles;
   }

   @Override
   public Title getTitle(Locale language)
   {
      return new TitleImpl(localeTitle);
   }

}
