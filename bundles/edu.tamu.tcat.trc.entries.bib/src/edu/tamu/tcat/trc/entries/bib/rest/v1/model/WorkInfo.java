package edu.tamu.tcat.trc.entries.bib.rest.v1.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.bib.AuthorList;
import edu.tamu.tcat.trc.entries.bib.AuthorReference;
import edu.tamu.tcat.trc.entries.bib.Title;
import edu.tamu.tcat.trc.entries.bib.TitleDefinition;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;


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
      // TODO make a more flexible tool for creating work identifiers

      WorkInfo result = new WorkInfo();
      result.id = w.getId();
      result.uri = "works/" + w.getId();
      result.title = constructLabel(w);
      result.pubYear = getNormalizedYear(w);

      result.summary = w.getSummary();

      AuthorList authors = w.getAuthors();
      authors.forEach(author ->
      {
         result.authors.add(new AuthorRefDV(author));
      });

      // TODO find earliest listed publication date (first edition)

      return result;
   }

   private static String constructLabel(Work w)
   {
      TitleDefinition titleDefn = w.getTitle();
      Set<Title> titles = titleDefn.getAlternateTitles();
      String name = getAuthorName(w);
      String pubDate = getNormalizedYear(w);

      StringBuilder sb = new StringBuilder();
      if (name != null)
         sb.append(name).append(pubDate == null ? ", " : " ");

      if (pubDate != null)
         sb.append("(").append(pubDate).append("): ");

      sb.append(getWorkTitle(titles));
      return sb.toString();
   }

   /** @return the author's last name (or best approximate) */
   private static String getAuthorName(Work w)
   {
      AuthorList authors = w.getAuthors();
      String name = null;
      if (authors.size() >= 0)
      {
         AuthorReference ref = authors.get(0);
         name = trimToNull(ref.getLastName());
         if (name == null)
            name = trimToNull(ref.getFirstName());

         if (name == null)
            name = trimToNull(ref.getName());
      }
      return name;
   }

   private static String getWorkTitle(Set<Title> titles)
   {
      String result = "no title available";
      if (!titles.isEmpty())
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

         result = title.getFullTitle();
      }

      return result;
   }

   /** @return the year this work was published. May be null */
   private static String getNormalizedYear(Work w)
   {
      LocalDate d = w.getEditions().stream()
            .map(ed ->
               ed.getPublicationInfo().getPublicationDate().getCalendar())
            .filter(pubDate ->
               pubDate != null)
            .min(LocalDate::compareTo)
            .orElse(null);

      if (d == null)
         return null;

      // correct for year-dates that were offset due to UTC conversion to Dec 31st.
      // HACK: should be pushed closer to data source and/or removed once data is updated
      int yr = d.getYear();
      int mo = d.getMonthValue();
      int day = d.getDayOfMonth();
      if (mo == 12 && day == 31)
      {
         yr++;             // assume that what was meant as just a year
      }

      return String.valueOf(yr);
   }

   private static String trimToNull(String value)
   {
      return (value == null || value.trim().isEmpty()) ? null : value.trim();
   }

   public WorkInfo()
   {
   }

}
