package edu.tamu.tcat.sda.catalog.works;


/**
 * Bibliographic description for a book, article, journal or other work. This is the main 
 * point of entry for working with bibliographic records. 
 */
public interface Work
{
   /**
    * @return A unique, persistent identifier for this work. 
    */
   String getId();
   
   /**
    * @return The authors of this work. 
    */
   AuthorList getAuthors();   
   
   /**
    * @return The title of this work.
    */
   TitleDefinition getTitle();
   
   /**
    * @return Secondary authors associated with this work. This corresponds to authors that 
    *    would typically be displayed after the title information, such as the translator of a 
    *    work. For example, in the entry Spinoza. <em>Tractatus Theologico-Politicus</em>. 
    *    Trans by Willis. 1862. Willis would be the 'outher authors'.
    *  
    */
   AuthorList getOtherAuthors();  
   
   /**
    * @return Details about when, where and by whom this work was published.
    */
   PublicationInfo getPublicationInfo();
   
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
