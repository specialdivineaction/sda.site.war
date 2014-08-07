package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;

public class TitleDefinitionImpl implements TitleDefinition
{
   private Set<TitleDV> titleDvs;

   public TitleDefinitionImpl(Set<TitleDV> titles)
   {
      this.titleDvs = titles;
   }

   @Override
   public Title getCanonicalTitle()
   {
      for(TitleDV title : titleDvs)
      {
         if(title.type.equals("canonical"))
            return new TitleImpl(title);
      }
      return null;
   }

   @Override
   public Title getShortTitle()
   {
      for(TitleDV title : titleDvs)
      {
         if(title.type.equals("short"))
            return new TitleImpl(title);
      }
      return null;
   }

   @Override
   public Title getTitle(Locale language)
   {
      for(TitleDV title : titleDvs)
      {
         if(title.type.equals("locale"))
            return new TitleImpl(title);
      }
      return null;
   }

   @Override
   public Set<Title> getAlternateTitles()
   {
      Set<Title> titles = new HashSet<Title>();

      for(TitleDV title : titleDvs)
      {
         titles.add(new TitleImpl(title));
      }

      return titles;
   }
}
