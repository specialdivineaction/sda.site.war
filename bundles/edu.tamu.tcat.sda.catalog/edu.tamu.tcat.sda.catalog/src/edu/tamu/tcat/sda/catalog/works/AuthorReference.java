package edu.tamu.tcat.sda.catalog.works;

import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;

/**
 * Links the author of a work to a biographical record of the person. This allows the 
 * bibliographic record to capture the name of the author as it appears on the work, along with
 * the role the author played in the creation of this work (e.g., author, translator, editor,
 * director, etc).
 */
public interface AuthorReference
{
   /**
    * @return Information about the person represented by this author. Will not be {@code null}. 
    *    For authors for whom no biographical record is available this should return a special
    *    person instance representing an unknown or unavailable person.
    */
   HistoricalFigure getAuthor();     // TODO need to flesh out how to reference unavailable people.
   
   /**
    * @return The name of the author as it appears on the work.
    */
   String getName();    // TODO need PersonName structured representation.
   
   /**
    * @return The role this person played in the creation of the work, for example, author, 
    *    translator, editor, director, etc.. This is an application specific value.
    */
   String getRole();
}
