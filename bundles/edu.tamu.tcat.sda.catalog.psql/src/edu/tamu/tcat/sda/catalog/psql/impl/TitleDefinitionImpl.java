package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.Locale;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;

public class TitleDefinitionImpl implements TitleDefinition
{

   private final TitleDV titleDV;
   
   public TitleDefinitionImpl(TitleDV title)
   {
      this.titleDV = title;
      
      
   }
   
   @Override
   public Title getCanonicalTitle()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Title getShortTitle()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<Title> getAlternateTitles()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Title getTitle(Locale language)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
