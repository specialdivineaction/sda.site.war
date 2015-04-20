package edu.tamu.tcat.trc.entries.bib.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.AuthorList;
import edu.tamu.tcat.trc.entries.bib.Edition;
import edu.tamu.tcat.trc.entries.bib.PublicationInfo;
import edu.tamu.tcat.trc.entries.bib.Title;
import edu.tamu.tcat.trc.entries.bib.TitleDefinition;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.postgres.model.AuthorListImpl;
import edu.tamu.tcat.trc.entries.bib.postgres.model.EditionImpl;
import edu.tamu.tcat.trc.entries.bib.postgres.model.TitleDefinitionImpl;


/**
 * Represents a work
 */
public class WorkDV
{
   public String id;
   public List<AuthorRefDV> authors;
   public Collection<TitleDV> titles;
   public List<AuthorRefDV> otherAuthors;
   public String series;
   public String summary;

   // HACK: old records may not have this field; set to empty set by default.
   public Collection<EditionDV> editions = new HashSet<>();

   public static Work instantiate(WorkDV dto)
   {
      return new WorkImpl(dto);
   }

   public static WorkDV create(Work work)
   {
      return new WorkDV(work);
   }

   public WorkDV()
   {
   }

   public WorkDV(Work work)
   {
      this.id = work.getId();
      this.authors = new ArrayList<>();
      work.getAuthors().forEach(ref -> authors.add(new AuthorRefDV(ref)));

      this.otherAuthors = new ArrayList<>();
      work.getOtherAuthors().forEach(ref -> otherAuthors.add(new AuthorRefDV(ref)));

      Collection<Title> altTitles = work.getTitle().getAlternateTitles();
      titles = altTitles.stream().map(title -> new TitleDV(title)).collect(Collectors.toSet());

      this.series = work.getSeries();
      this.summary = work.getSummary();

      this.editions = work.getEditions().parallelStream()
            .map((e) -> new EditionDV(e))
            .collect(Collectors.toSet());
   }



   private static class WorkImpl implements Work
   {
         private final String id;
         private final AuthorListImpl authors;
         private final AuthorListImpl otherAuthors;
         private final TitleDefinitionImpl title;
         private final String series;
         private final String summary;
         private final Collection<Edition> editions;

         public WorkImpl(WorkDV workDV)
         {
            this.id = workDV.id;

            this.authors = new AuthorListImpl(workDV.authors);
            this.title = new TitleDefinitionImpl(workDV.titles);
            this.otherAuthors = new AuthorListImpl(workDV.otherAuthors);
            this.series = workDV.series;
            this.summary = workDV.summary;
            this.editions = workDV.editions.parallelStream()
                  .map((e) -> new EditionImpl(e))
                  .collect(Collectors.toSet());
         }

         @Override
         public String getId()
         {
            return id;
         }

         @Override
         public AuthorList getAuthors()
         {
            return authors;
         }

         @Override
         public TitleDefinition getTitle()
         {
            return title;
         }

         @Override
         public AuthorList getOtherAuthors()
         {
            return otherAuthors;
         }

         @Override
         public PublicationInfo getPublicationInfo()
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public String getSeries()
         {
            return series;
         }

         @Override
         public String getSummary()
         {
            return summary;
         }

         @Override
         public Collection<Edition> getEditions()
         {
            return editions;
         }

         @Override
         public Edition getEdition(String editionId) throws NoSuchCatalogRecordException
         {
            for (Edition edition : editions) {
               if (edition.getId().equals(editionId)) {
                  return edition;
               }
            }

            throw new NoSuchCatalogRecordException("Unable to find edition [" + editionId + "] in work [" + id + "].");
         }

   }
}
