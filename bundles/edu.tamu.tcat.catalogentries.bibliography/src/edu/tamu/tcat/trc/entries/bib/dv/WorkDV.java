package edu.tamu.tcat.trc.entries.bib.dv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.bib.AuthorReference;
import edu.tamu.tcat.trc.entries.bib.Title;
import edu.tamu.tcat.trc.entries.bib.Work;


/**
 * Represents a work
 */
public class WorkDV
{
   public String id;
   public List<AuthorRefDV> authors;
   public Collection<TitleDV> titles;
   public List<AuthorRefDV> otherAuthors;
//   public PublicationInfoDV pubInfo;
   public String series;
   public String summary;

   // HACK: old records may not have this field; set to empty set by default.
   public Collection<EditionDV> editions = new HashSet<>();

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
      Collection<Title> altTitles = work.getTitle().getAlternateTitles();
      for(Title title : altTitles)
      {
         titles.add(new TitleDV(title));
      }

//      this.pubInfo = new PublicationInfoDV(work.getPublicationInfo());
      this.series = work.getSeries();
      this.summary = work.getSummary();

      this.editions = work.getEditions().parallelStream()
            .map((e) -> new EditionDV(e))
            .collect(Collectors.toSet());
   }

   public WorkDV()
   {
   }
}
