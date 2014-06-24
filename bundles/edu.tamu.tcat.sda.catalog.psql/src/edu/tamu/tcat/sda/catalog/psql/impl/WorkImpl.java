package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.sda.catalog.works.AuthorList;
import edu.tamu.tcat.sda.catalog.works.PublicationInfo;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorListDV;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class WorkImpl implements Work
{
   private final WorkDV work;
   public WorkImpl(WorkDV workDV)
   {
      this.work = workDV;
   }
   
   @Override
   public AuthorList getAuthors()
   {
      return new AuthorListImpl(work.authors);
   }

   @Override
   public TitleDefinition getTitle()
   {
      return new TitleDefinitionImpl(work.title);
   }

   @Override
   public AuthorList getOtherAuthors()
   {
      return new AuthorListImpl(work.otherAuthors);
   }

   @Override
   public PublicationInfo getPublicationInfo()
   {
      return new PublicationImpl(work.pubInfo);
   }

   @Override
   public String getSeries()
   {
      return work.series;
   }

   @Override
   public String getSummary()
   {
      return work.summary;
   }

   @Override
   public String getId()
   {
      return work.id;
   }

}
