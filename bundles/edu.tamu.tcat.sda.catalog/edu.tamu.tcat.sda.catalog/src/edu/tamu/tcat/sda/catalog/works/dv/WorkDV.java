package edu.tamu.tcat.sda.catalog.works.dv;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.Work;


/**
 * Represents a work
 */
public class WorkDV
{
   public String id;
   public List<AuthorRefDV> authors;
   public Set<TitleDV> titles;
   public List<AuthorRefDV> otherAuthors;
   public PublicationInfoDV pubInfo;
   public String series;
   public String summary;

   public WorkDV(Work work)
   {
      this.id = work.getId();
      this.authors = new ArrayList<>();
      for (AuthorReference ref : work.getAuthors())
      {
         authors.add(new AuthorRefDV(ref));
      }

      this.otherAuthors = new ArrayList<>();
      for (AuthorReference ref : work.getOtherAuthors())
      {
         otherAuthors.add(new AuthorRefDV(ref));
      }

      titles = new HashSet<>();
      Set<Title> altTitles = work.getTitle().getAlternateTitles();
      for(Title title : altTitles)
      {
         titles.add(new TitleDV(title));
      }

      this.pubInfo = new PublicationInfoDV(work.getPublicationInfo());
      this.series = work.getSeries();
      this.summary = work.getSummary();
   }

   public WorkDV()
   {
   }
}
