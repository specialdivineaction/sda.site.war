package edu.tamu.tcat.sda.catalog.psql;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.EditWorkCommand;
import edu.tamu.tcat.sda.catalog.works.EditionMutator;
import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.DateDescriptionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class EditWorkCommandImpl implements EditWorkCommand
{

   private WorkDV work;
   private Function<WorkDV, Future<String>> commitHook;

   public EditWorkCommandImpl(WorkDV work)
   {
      this.work = work;
   }

   public void setCommitHook(Function<WorkDV, Future<String>> hook)
   {
      commitHook = hook;
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
   public void setAuthors(List<AuthorReference> authors)
   {
      work.authors = authors.stream()
            .map((ref) -> new AuthorRefDV(ref))
            .collect(Collectors.toList());
   }

   @Override
   public void setOtherAuthors(List<AuthorReference> authors)
   {
      work.otherAuthors = authors.stream()
            .map((ref) -> new AuthorRefDV(ref))
            .collect(Collectors.toList());
   }

   @Override
   public void setTitles(List<Title> titles)
   {
      // TODO: Should work.titles be a list instead of a set, or
      //       Should the argument to this function be a set?
      work.titles = titles.stream()
            .map((title) -> new TitleDV(title))
            .collect(Collectors.toSet());
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
   public EditionMutator getEditionMutator()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Future<String> execute()
   {
      return commitHook.apply(work);
   }

}
