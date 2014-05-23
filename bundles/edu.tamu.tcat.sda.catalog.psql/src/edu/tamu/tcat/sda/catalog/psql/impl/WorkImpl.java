package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.List;

import edu.tamu.tcat.sda.catalog.works.AuthorList;
import edu.tamu.tcat.sda.catalog.works.PublicationInfo;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDv;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
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
      return new AuthorListImpl(work.authors.iterator());
   }

   @Override
   public TitleDefinition getTitle()
   {
      List<TitleDV> titles = work.titles;
      return null;
   }

   @Override
   public AuthorList getOtherAuthors()
   {
      return new AuthorListImpl(work.otherAuthors.iterator());
   }

   @Override
   public PublicationInfo getPublicationInfo()
   {
      return new PublicationImpl(work.pubInfo);
   }

   @Override
   public String getSeries()
   {
      
      return null;
   }

   @Override
   public String getSummary()
   {
      return work.summary;
   }

}
