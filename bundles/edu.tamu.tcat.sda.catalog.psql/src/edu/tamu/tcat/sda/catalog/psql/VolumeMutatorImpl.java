package edu.tamu.tcat.sda.catalog.psql;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import edu.tamu.tcat.sda.catalog.works.VolumeMutator;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;

public class VolumeMutatorImpl implements VolumeMutator
{

   private final VolumeDV volume;


   public VolumeMutatorImpl(VolumeDV volume)
   {
      this.volume = volume;
   }


   @Override
   public void setAll(VolumeDV volume)
   {
      setVolume(volume.volume);
      setAuthors(volume.authors);
      setTitles(volume.titles);
      setSummary(volume.summary);
      setSeries(volume.series);
      setImages(volume.images);
      setTags(volume.tags);
      setNotes(volume.notes);
   }

   @Override
   public void setVolume(String volume)
   {
      this.volume.volume = volume;
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      volume.authors = authors;
   }

   @Override
   public void setTitles(List<TitleDV> titles)
   {
      volume.titles = titles;
   }

   @Override
   public void setSummary(String summary)
   {
      volume.summary = summary;
   }

   @Override
   public void setSeries(String series)
   {
      volume.series = series;
   }

   @Override
   public void setImages(List<URI> images)
   {
      volume.images = images;
   }

   @Override
   public void setTags(Collection<String> tags)
   {
      volume.tags = tags;
   }

   @Override
   public void setNotes(Collection<String> notes)
   {
      volume.notes = notes;
   }

}
