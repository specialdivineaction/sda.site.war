package edu.tamu.tcat.sda.catalog.psql;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.EditWorkCommand;
import edu.tamu.tcat.sda.catalog.works.EditionMutator;
import edu.tamu.tcat.sda.catalog.works.Title;
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

   }

   @Override
   public void setSummary(String summary)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setAuthors(List<AuthorReference> authors)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setOtherAuthors(List<AuthorReference> authors)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setTitles(List<Title> titles)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setPublicationDate(Date pubDate)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setPublicationDateDisplay(String display)
   {
      // TODO Auto-generated method stub

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
