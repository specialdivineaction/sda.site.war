package edu.tamu.tcat.sda.catalog.works;

import java.util.List;

/**
 * Bibliographic description for a book, article, journal or other work. This is the main 
 * point of entry for working with bibliographic records. 
 */
public interface Work
{
   /**
    * @return The authors of this work. 
    */
   List<AuthorReference> getAuthors();    // TODO create AuthorList type
   
   /**
    * @return The title of this work.
    */
   Title getTitle();
   
   /**
    * @return A defined series of related works, typically published by a single publishers and 
    *    issued under the direction of a series editor or editors.
    */
   String getSeries();                    // TODO make series a first-level entity
   
   /**
    * @return A brief summary of this work.
    */
   String getSummary();
}
