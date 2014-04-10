package edu.tamu.tcat.sda.catalog.works;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A simple textual document that may include some light HT 
 *
 */
public interface SimpleDocument
{
   /**
    * @return A unique identifier for this document.
    */
   UUID getId();
   
   /**
    * @return The revision number of this document.
    */
   long getRevision();
   
   /**
    * @return The account identifier of the person or people responsible for creating this
    *    this document. 
    */
   Set<Long> getAuthorId();
   
   /**
    * @return The text of this document including any embedded markup.
    */
   String getText();
   
   
   Map<Long, DocumentRevision> getRevisions();
   
   

}
