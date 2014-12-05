package edu.tamu.tcat.sda.catalog.works;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;

public interface VolumeMutator
{
   void setAll(VolumeDV volume);

   void setVolumeNumber(String volumeNumber);
   void setAuthors(List<AuthorRefDV> authors);
   void setTitles(Collection<TitleDV> titles);
   void setOtherAuthors(List<AuthorRefDV> otherAuthors);
   void setSummary(String summary);
   void setSeries(String series);
   void setImages(List<URI> images);
   void setTags(Collection<String> tags);
   void setNotes(Collection<String> notes);

   /**
   *
   * @return The unique identifier for the volume that this mutator modifies.
   *         Will not be {@code null}. For newly created volumes, this identifier
   *         will be assigned when the java object is first created rather than when
   *         the volume is committed to the persistence layer.
   */
  String getId();
}
