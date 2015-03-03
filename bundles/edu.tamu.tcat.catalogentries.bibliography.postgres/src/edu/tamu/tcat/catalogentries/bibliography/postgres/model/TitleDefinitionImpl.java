package edu.tamu.tcat.catalogentries.bibliography.postgres.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import edu.tamu.tcat.catalogentries.bibliography.Title;
import edu.tamu.tcat.catalogentries.bibliography.TitleDefinition;
import edu.tamu.tcat.catalogentries.bibliography.dv.TitleDV;

public class TitleDefinitionImpl implements TitleDefinition
{
   private Set<Title> titles = new HashSet<>();

   public TitleDefinitionImpl(Collection<TitleDV> titles)
   {
      for (TitleDV title : titles)
      {
         this.titles.add(new TitleImpl(title));
      }
   }

   @Override
   public Title getCanonicalTitle()
   {

	   throw new UnsupportedOperationException();
   }

   @Override
   public Title getShortTitle()
   {
	   throw new UnsupportedOperationException();
   }

   @Override
   public Set<Title> getAlternateTitles()
   {

      return Collections.unmodifiableSet(titles);
   }

   @Override
   public Title getTitle(Locale language)
   {
	   throw new UnsupportedOperationException();
   }
}
