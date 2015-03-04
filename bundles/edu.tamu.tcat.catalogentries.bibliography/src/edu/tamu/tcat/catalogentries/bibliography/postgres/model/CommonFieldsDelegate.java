package edu.tamu.tcat.catalogentries.bibliography.postgres.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.catalogentries.bibliography.AuthorReference;
import edu.tamu.tcat.catalogentries.bibliography.Title;
import edu.tamu.tcat.catalogentries.bibliography.dv.AuthorRefDV;
import edu.tamu.tcat.catalogentries.bibliography.dv.TitleDV;

/**
 * A helper class to manage fields that are common across different levels of the Work taxonomy.
 *
 */
public class CommonFieldsDelegate
{

   private List<AuthorReference> authors = new ArrayList<>();
   private Collection<Title> titles = new HashSet<>();
   private List<AuthorReference> otherAuthors = new ArrayList<>();

   private String summary;

   public CommonFieldsDelegate()
   {
      // TODO Auto-generated constructor stub
   }
   public CommonFieldsDelegate(List<AuthorRefDV> authors, Collection<TitleDV> titles, List<AuthorRefDV> others, String summary)
   {

      this.authors = authors.stream()
            .map((a) -> new AuthorReferenceImpl(a))
            .collect(Collectors.toList());

      this.titles = titles.parallelStream()
            .map((t) -> new TitleImpl(t))
            .collect(Collectors.toSet());

      this.otherAuthors = others.stream()
            .map((a) -> new AuthorReferenceImpl(a))
            .collect(Collectors.toList());

      this.summary = summary;
   }

   public List<AuthorReference> getAuthors()
   {
      return Collections.unmodifiableList(authors);
   }

   public Collection<Title> getTitles()
   {
      return Collections.unmodifiableCollection(titles);
   }

   public List<AuthorReference> getOtherAuthors()
   {
      return Collections.unmodifiableList(otherAuthors);
   }

   public String getSummary()
   {
      return summary;
   }
}
