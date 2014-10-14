package edu.tamu.tcat.sda.catalog.works;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public interface EditWorkCommand
{
   // TODO: Should these methods take in full models or data vehicles?
   //       Should there be methods to handle both data types?

   void setAll(WorkDV work);

   // TODO: Any field that is a collection of models should eventually use mutators.

   void setAuthors(List<AuthorRefDV> authors);
   void setTitles(Collection<TitleDV> titles);
   void setOtherAuthors(List<AuthorRefDV> authors);
   void setSeries(String series);
   void setSummary(String summary);

   @Deprecated // this is a property of an edition.
   void setPublicationDate(Date pubDate);

   @Deprecated // this is a property of an edition.
   void setPublicationDateDisplay(String display);

   /**
    * Creates an edition mutator to update fields on an existing edition of this work.
    *
    * @param id The ID of a contained edition.
    * @return A mutator for the given edition ID.
    */
   EditionMutator editEdition(String id) throws NoSuchCatalogRecordException;

   /**
    * Creates an edition mutator for a new edition of this work.
    *
    * @return
    */
   EditionMutator createEdition();

   Future<String> execute();
}
