package edu.tamu.tcat.sda.catalog.psql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Logger;

import edu.tamu.tcat.sda.catalog.IdFactory;
import edu.tamu.tcat.sda.catalog.InvalidDataException;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.works.EditWorkCommand;
import edu.tamu.tcat.sda.catalog.works.EditionMutator;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class EditWorkCommandImpl implements EditWorkCommand
{
   private static final Logger logger = Logger.getLogger(EditWorkCommandImpl.class.getName());

   private final WorkDV work;
   private final IdFactory idFactory;

   private Function<WorkDV, Future<String>> commitHook;

   EditWorkCommandImpl(WorkDV work, IdFactory idFactory)
   {
      this.work = work;
      this.idFactory = idFactory;
   }

   public void setCommitHook(Function<WorkDV, Future<String>> hook)
   {
      commitHook = hook;
   }

   @Override
   public void setAll(WorkDV work) throws InvalidDataException
   {
      setSeries(work.series);
      setSummary(work.summary);
      setAuthors(work.authors);
      setOtherAuthors(work.otherAuthors);
      setTitles(work.titles);

      setEditions(work.editions);
   }

   private void setEditions(Collection<EditionDV> editions)
   {
      work.editions.clear();
      for (EditionDV edition : editions) {
         EditionMutator mutator;

         try {
            mutator = (null == edition.id) ? createEdition() : editEdition(edition.id);
         }
         catch (NoSuchCatalogRecordException e) {
            throw new InvalidDataException("Failed to edit existing edition. A supplied edition contains an id [" + edition.id + "], but the identified edition cannot be retrieved for editing.", e);
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

//   @Override
//   public void setPublicationDate(Date pubDate)
//   {
//      if (null == work.pubInfo) {
//         work.pubInfo = new PublicationInfoDV();
//      }
//
//      if (null == work.pubInfo.date) {
//         work.pubInfo.date = new DateDescriptionDV();
//      }
//
//      work.pubInfo.date.value = pubDate;
//   }
//
//   @Override
//   public void setPublicationDateDisplay(String display)
//   {
//      if (null == work.pubInfo) {
//         work.pubInfo = new PublicationInfoDV();
//      }
//
//      if (null == work.pubInfo.date) {
//         work.pubInfo.date = new DateDescriptionDV();
//      }
//
//      work.pubInfo.date.display = display;
//   }

   @Override
   public EditionMutator createEdition()
   {
      EditionDV edition = new EditionDV();
      edition.id = idFactory.getNextId(PsqlWorkRepo.getContext(work));
      work.editions.add(edition);

      // create a supplier to generate volume IDs
      return new EditionMutatorImpl(edition, () -> idFactory.getNextId(PsqlWorkRepo.getContext(work, edition)));
   }

   @Override
   public EditionMutator editEdition(String id) throws NoSuchCatalogRecordException
   {
      for (EditionDV edition : work.editions) {
         if (edition.id.equals(id)) {
            // create a supplier to generate volume IDs
            return new EditionMutatorImpl(edition, () -> idFactory.getNextId(PsqlWorkRepo.getContext(work, edition)));
         }
      }

      throw new NoSuchCatalogRecordException("Unable to find edition with id [" + id + "].");
   }

   @Override
   public Future<String> execute()
   {
      Objects.requireNonNull(commitHook, "");

      return commitHook.apply(work);
   }

}
