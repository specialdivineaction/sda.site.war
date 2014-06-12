package edu.tamu.tcat.sda.catalog.people;

import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

/**
 * 
 *
 */
public interface HistoricalFigureRepository
{
   Iterable<HistoricalFigure> listHistoricalFigures();
   
   HistoricalFigure getPerson(long personId);
   
   /**
    * Creates a new entry for the supplied historical figure. Note that no de-duplication will
    * be performed. If this person (or a similar person) has already been added, a new entry
    * will be created.
    * 
    * <p>This method will execute asynchronously. Upon success, it will pass an instance of 
    * {@link HistoricalFigure} representing the create person to the observer. On failure, it 
    * will supply an error message and optionally, a exception associated with the failure. 
    * 
    * @param histFigure A data vehicle containing the information for the person to create. 
    * @param observer An optional observer that will be notified upon success or failure of 
    *       this operation.
    */
   void create(HistoricalFigureDV histFigure, DataUpdateObserver<HistoricalFigure> observer);
   
   void update(HistoricalFigureDV histFigure, DataUpdateObserver<HistoricalFigure> observer);
}