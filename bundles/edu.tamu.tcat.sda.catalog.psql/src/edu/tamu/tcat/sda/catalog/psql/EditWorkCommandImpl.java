package edu.tamu.tcat.sda.catalog.psql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.works.EditWorkCommand;
import edu.tamu.tcat.sda.catalog.works.EditionMutator;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.DateDescriptionDV;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class EditWorkCommandImpl implements EditWorkCommand
{

   private final WorkDV work;
   private final IdProvider editionIdProvider;

   private Function<WorkDV, Future<String>> commitHook;

   public EditWorkCommandImpl(WorkDV work, IdProvider editionIdProvider)
   {
      this.work = work;
      this.editionIdProvider = editionIdProvider;
   }

   public void setCommitHook(Function<WorkDV, Future<String>> hook)
   {
      commitHook = hook;
   }

   @Override
   public void setAll(WorkDV work)
   {
      setSeries(work.series);
      setSummary(work.summary);
      setAuthors(work.authors);
      setOtherAuthors(work.otherAuthors);
      setTitles(work.titles);
      setPublicationDate(work.pubInfo.date.value);
      setPublicationDateDisplay(work.pubInfo.date.display);

      for (EditionDV edition : work.editions) {
         EditionMutator mutator;

         try {
            mutator = (null == edition.id) ? createEdition() : editEdition(edition.id);
         }
         catch (NoSuchCatalogRecordException e) {
            // TODO: Log warning message
            mutator = createEdition();
         }

         mutator.setAll(edition);
      }
   }

   @Override
   public void setSeries(String series)
   {
      work.series = series;
   }

   @Override
   public void setSummary(String summary)
   {
      work.summary = summary;
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      work.authors = new ArrayList<>(authors);
   }

   @Override
   public void setOtherAuthors(List<AuthorRefDV> authors)
   {
      work.otherAuthors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDV> titles)
   {
      work.titles = new HashSet<>(titles);
   }

   @Override
   public void setPublicationDate(Date pubDate)
   {
      if (null == work.pubInfo) {
         work.pubInfo = new PublicationInfoDV();
      }

      if (null == work.pubInfo.date) {
         work.pubInfo.date = new DateDescriptionDV();
      }

      work.pubInfo.date.value = pubDate;
   }

   @Override
   public void setPublicationDateDisplay(String display)
   {
      if (null == work.pubInfo) {
         work.pubInfo = new PublicationInfoDV();
      }

      if (null == work.pubInfo.date) {
         work.pubInfo.date = new DateDescriptionDV();
      }

      work.pubInfo.date.display = display;
   }

   @Override
   public EditionMutator createEdition()
   {
      EditionDV edition = new EditionDV();
      edition.id = editionIdProvider.nextId();
      work.editions.add(edition);
      return new EditionMutatorImpl(edition);
   }

   @Override
   public EditionMutator editEdition(String id) throws NoSuchCatalogRecordException
   {
      for (EditionDV edition : work.editions) {
         if (edition.id.equals(id)) {
            return new EditionMutatorImpl(edition);
         }
      }

      throw new NoSuchCatalogRecordException("Unable to find edition with id [" + id + "].");
   }

   @Override
   public Future<String> execute()
   {
      return commitHook.apply(work);
   }

}
