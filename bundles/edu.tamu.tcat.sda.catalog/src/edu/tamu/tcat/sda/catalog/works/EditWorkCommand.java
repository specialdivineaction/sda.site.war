package edu.tamu.tcat.sda.catalog.works;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

public interface EditWorkCommand // extends Callable<Work>
{
   public void setId(String id);
   public void setSeries(String series);
   public void setSummary(String summary);

   public void setAuthors(List<AuthorReference> authors);

   public void setTitles(List<Title> titles);

   public void setPublicationDate(Date pubDate);
   public void setPublicationDateDisplay(String display);

   public EditionMutator getEditionMutator();

   Future<String> execute();
}
