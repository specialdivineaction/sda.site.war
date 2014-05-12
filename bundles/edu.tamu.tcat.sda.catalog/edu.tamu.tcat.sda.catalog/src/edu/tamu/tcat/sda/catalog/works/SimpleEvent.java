package edu.tamu.tcat.sda.catalog.works;

import java.util.Date;

/**
 * Provides a representation of a historical event that occurs as a specific 
 * place and date.
 */
public interface SimpleEvent
{
   // NOTE This will be extended and revised significantly as we flesh out the notion 
   //      of events. Should perhaps be changed to be a more simple identifier and we can 
   //      use other controls to attach additional info, but I think, a start date, end date, 
   //      location, title and description are probably a good basic description for the 
   //      identifier token. We might also add in some user-defined type information.
   
   /**
    * @return The date at when this event happened.
    */
   Date getDate();
   
   /**
    * @return The location where this event happened.
    */
   String getLocation();
   
   /**
    * @return A brief description of this event.
    */
   String getNotes();
}