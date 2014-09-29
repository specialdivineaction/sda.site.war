package edu.tamu.tcat.sda.catalog.repo.work;

import java.util.Date;
import java.util.List;

import edu.tamu.tcat.sda.catalog.repo.Command;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.Title;

public interface EditWorkCommand extends Command
{
   public void setId(String id);
   public void setSeries(String series);
   public void setSummary(String summary);

   public void setAuthors(List<AuthorReference> authors);

   public void setTitles(List<Title> titles);

   public void setPublicationDate(Date pubDate);
   public void setPublicationDateDisplay(String display);

   public EditionMutator getEditionMutator();
}
