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

   void setVolume(String volume);
   void setAuthors(List<AuthorRefDV> authors);
   void setTitles(List<TitleDV> titles);
   void setSummary(String summary);
   void setSeries(String series);
   void setImages(List<URI> images);
   void setTags(Collection<String> tags);
   void setNotes(Collection<String> notes);
}
