package edu.tamu.tcat.sda.catalog.works.dv;


/**
 * Represents a work 
 */
public class WorkDV
{
   public String id;
   public AuthorListDV authors;
   public TitleDefinitionDV title;
   public AuthorListDV otherAuthors;
   public PublicationInfoDV pubInfo;
   public String series;
   public String summary; 
   
   public WorkDV()
   {
   }
}
