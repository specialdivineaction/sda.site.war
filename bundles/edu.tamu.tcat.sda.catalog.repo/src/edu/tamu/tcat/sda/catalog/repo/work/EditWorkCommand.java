package edu.tamu.tcat.sda.catalog.repo.work;

import java.util.Date;
import java.util.List;

import edu.tamu.tcat.sda.catalog.repo.Command;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.Title;

public interface EditWorkCommand extends Command
{
   void setSeries(String series);
   void setSummary(String summary);

   void setAuthors(List<AuthorReference> authors);

   void setTitles(List<Title> titles);

   void setPublicationDate(Date pubDate);
   void setPublicationDateDisplay(String display);

   EditionMutator getEditionMutator();
}
