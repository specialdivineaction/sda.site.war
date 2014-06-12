package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDefinitionDV;


public class TitleDefinitionImpl implements TitleDefinition
{

   private final TitleDefinitionDV titleDef;
   
   public TitleDefinitionImpl(TitleDefinitionDV title)
   {
      this.titleDef = title;
   }
   
   @Override
   public Title getCanonicalTitle()
   {
      return new TitleImpl(titleDef.canonicalTitle);
   }

   @Override
   public Title getShortTitle()
   {
      return new TitleImpl(titleDef.shortTitle);
   }

   @Override
   public Set<Title> getAlternateTitles()
   {
      Set<TitleDV> alternateTitles = titleDef.alternateTitles;
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
      return new TitleImpl(titleDef.localeTitle);
   }

}
