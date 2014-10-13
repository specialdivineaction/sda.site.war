package edu.tamu.tcat.sda.catalog.works;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;

public interface EditionMutator
{
   void setAll(EditionDV edition);

   void setAuthors(List<AuthorRefDV> authors);
   void setTitles(List<TitleDV> titles);
   void setOtherAuthors(List<AuthorRefDV> otherAuthors);
   void setEdition(String edition);
   void setPublicationInfo(PublicationInfoDV pubInfo);
   void setSummary(String summary);
   void setSeries(String series);
   void setImages(List<URI> images);
   void setTags(Collection<String> tags);
   void setNotes(Collection<String> notes);

   VolumeMutator createVolume();
   VolumeMutator editVolume(String id) throws NoSuchCatalogRecordException;
}
