package edu.tamu.tcat.sda.catalog.works;

import java.util.Collection;
import java.util.List;

import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;

public interface EditionMutator
{
   // TODO add JavaDoc!!
   void setAll(EditionDV edition);

   void setAuthors(List<AuthorRefDV> authors);
   void setTitles(Collection<TitleDV> titles);
   void setOtherAuthors(List<AuthorRefDV> otherAuthors);
   void setEditionName(String editionName);
   void setPublicationInfo(PublicationInfoDV pubInfo);
   void setSeries(String series);
   void setSummary(String summary);

   VolumeMutator createVolume();
   VolumeMutator editVolume(String id) throws NoSuchCatalogRecordException;

   /**
    *
    * @return The unique identifier for the edition that this mutator modifies.
    *         Will not be {@code null}. For newly created editions, this identifier
    *         will be assigned when the java object is first created rather than when
    *         the edition is committed to the persistence layer.
    */
   String getId();
}
