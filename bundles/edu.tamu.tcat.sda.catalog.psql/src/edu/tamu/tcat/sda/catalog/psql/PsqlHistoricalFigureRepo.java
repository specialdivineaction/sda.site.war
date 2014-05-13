package edu.tamu.tcat.sda.catalog.psql;

import java.util.Set;

import edu.tamu.tcat.sda.catalog.events.HistoricalEvent;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigureRepository;
import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.ds.DataUpdateObserver;

public class PsqlHistoricalFigureRepo implements HistoricalFigureRepository
{

   @Override
   public Iterable<HistoricalFigure> listHistoricalFigures()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void create(HistoricalFigureDV histFigure, DataUpdateObserver<HistoricalFigure> observer)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void update(HistoricalFigureDV histFigure, DataUpdateObserver<HistoricalFigure> observer)
   {
      // TODO Auto-generated method stub
      
   }

   private static class HistoricalFigureRef implements HistoricalFigure
   {

      @Override
      public String getId()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public PersonName getCanonicalName()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Set<PersonName> getAlternativeNames()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public HistoricalEvent getBirth()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public HistoricalEvent getDeath()
      {
         // TODO Auto-generated method stub
         return null;
      }
      
   }
}
