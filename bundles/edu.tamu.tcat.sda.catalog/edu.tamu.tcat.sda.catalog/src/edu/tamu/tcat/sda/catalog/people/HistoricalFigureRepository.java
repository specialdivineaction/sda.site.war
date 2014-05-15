package edu.tamu.tcat.sda.catalog.people;

import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.ds.DataUpdateObserver;

public interface HistoricalFigureRepository
{
   Iterable<HistoricalFigure> listHistoricalFigures();
   
   void create(HistoricalFigureDV histFigure, DataUpdateObserver<HistoricalFigure> observer);
   
   void update(HistoricalFigureDV histFigure, DataUpdateObserver<HistoricalFigure> observer);
}