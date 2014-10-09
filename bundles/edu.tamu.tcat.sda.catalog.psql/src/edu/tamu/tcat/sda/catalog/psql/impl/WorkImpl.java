package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import edu.tamu.tcat.sda.catalog.works.AuthorList;
import edu.tamu.tcat.sda.catalog.works.Edition;
import edu.tamu.tcat.sda.catalog.works.PublicationInfo;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class WorkImpl implements Work
{
   private final String id;
   private final AuthorListImpl authors;
   private final AuthorListImpl otherAuthors;
   private final TitleDefinitionImpl title;
   private final PublicationImpl publication;
   private final String series;
   private final String summary;
   private final Collection<Edition> editions;

   public WorkImpl(WorkDV workDV)
   {
      this.id = workDV.id;

      this.authors = new AuthorListImpl(workDV.authors);
      this.title = new TitleDefinitionImpl(workDV.titles);
      this.otherAuthors = new AuthorListImpl(workDV.otherAuthors);
      this.publication = new PublicationImpl(workDV.pubInfo);
      this.series = workDV.series;
      this.summary = workDV.summary;
      this.editions = workDV.editions.stream().unordered()
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
      return publication;
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

}
