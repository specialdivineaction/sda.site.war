package edu.tamu.tcat.sda.catalog.works.dv;

import edu.tamu.tcat.sda.catalog.works.Work;


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
   
   public WorkDV(Work work)
   {
      this.id = work.getId();
      this.authors = new AuthorListDV(work.getAuthors());
      this.title = new TitleDefinitionDV(work.getTitle());
      this.otherAuthors = new AuthorListDV(work.getOtherAuthors());
      this.pubInfo = new PublicationInfoDV(work.getPublicationInfo());
      this.series = work.getSeries();
      this.summary = work.getSummary();
   }
   
   public WorkDV()
   {
   }
}
