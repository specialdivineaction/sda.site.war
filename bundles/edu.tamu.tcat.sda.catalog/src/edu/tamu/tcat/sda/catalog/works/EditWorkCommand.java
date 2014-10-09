package edu.tamu.tcat.sda.catalog.works;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

public interface EditWorkCommand // extends Callable<Work>
{
   // TODO: Should these methods take in full models or data vehicles?
   //       Should there be methods to handle both data types?

   void setAuthors(List<AuthorReference> authors);
   void setTitles(List<Title> titles);
   // TODO: Should these methods take in full models or data vehicles?
   //       Should there be methods to handle both data types?

   void setOtherAuthors(List<AuthorReference> authors);
   void setSeries(String series);
   void setSummary(String summary);

   // TODO: Should these methods take in full models or data vehicles?
   //       Should there be methods to handle both data types?

   @Deprecated // this is a property of an edition.
   void setPublicationDate(Date pubDate);

   @Deprecated // this is a property of an edition.
   void setPublicationDateDisplay(String display);

   EditionMutator getEditionMutator();

   Future<String> execute();
}
