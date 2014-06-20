package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameDV;

public class HistoricalFigureImpl implements HistoricalFigure
{

   HistoricalFigureDV figureRef;
   Set<PersonNameDV> people;

   public HistoricalFigureImpl(HistoricalFigureDV figure)
   {
      this.figureRef = figure;
      this.people = figureRef.people;
   }

   @Override
   public String getId()
   {
      return figureRef.id;
   }

   @Override
   public PersonName getCanonicalName()
   {
      return null;
   }

   @Override
   public Set<PersonName> getAlternativeNames()
   {
      Set<PersonName> personSet = new HashSet<PersonName>();

      for (PersonNameDV person : people)
      {
         personSet.add(new PersonNameImpl(person));
      }

      return personSet;
   }

   @Override
   public Date getBirth()
   {

      return figureRef.birth;
   }

   @Override
   public Date getDeath()
   {
      return figureRef.death;
   }
}
