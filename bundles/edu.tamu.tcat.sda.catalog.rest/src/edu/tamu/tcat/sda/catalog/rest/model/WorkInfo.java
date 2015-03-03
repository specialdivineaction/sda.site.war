package edu.tamu.tcat.sda.catalog.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.catalogentries.bibliography.AuthorList;
import edu.tamu.tcat.catalogentries.bibliography.Title;
import edu.tamu.tcat.catalogentries.bibliography.TitleDefinition;
import edu.tamu.tcat.catalogentries.bibliography.Work;
import edu.tamu.tcat.catalogentries.bibliography.dv.AuthorRefDV;


/**
 * JSON serializable summary information about a work. Intended to be
 * returned when only a brief summary of the work is required to save
 * data transfer and parsing resources.
 *
 */
public class WorkInfo
{
   public String id;
   public String uri;
   public List<AuthorRefDV> authors = new ArrayList<>();
   public String title;
   public String summary;
   public String pubYear = null;

   public static WorkInfo create(Work w)
   {
      WorkInfo result = new WorkInfo();
      result.id = w.getId();
      result.uri = "tbd";     // TODO implement this
      TitleDefinition titleDefn = w.getTitle();
      Set<Title> titles = titleDefn.getAlternateTitles();
      if (titles.isEmpty())
      {
         result.title = "no title available";
      }
      else
      {
         Title title = titles.parallelStream()
                                  .filter(t -> t.getType().equalsIgnoreCase("short"))
                                  .findAny()
                                  .orElse(null);

         if (title == null)
            title = titles.parallelStream()
                              .filter(t -> t.getType().equalsIgnoreCase("canonical"))
                              .findAny()
                              .orElse(titles.iterator().next());

         result.title = title.getFullTitle();
      }

      result.summary = w.getSummary();    // TODO trim to first sentence

      AuthorList authors = w.getAuthors();
      authors.forEach(author ->
      {
         result.authors.add(new AuthorRefDV(author));
      });

      // TODO find earliest listed publication date (first edition)

      return result;
   }

   public WorkInfo()
   {
   }

}
