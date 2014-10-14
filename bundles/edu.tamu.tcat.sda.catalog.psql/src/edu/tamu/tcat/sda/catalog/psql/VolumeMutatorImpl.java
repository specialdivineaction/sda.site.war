package edu.tamu.tcat.sda.catalog.psql;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
      setVolumeNumber(volume.volumeNumber);
      setAuthors(volume.authors);
      setTitles(volume.titles);
      setOtherAuthors(volume.otherAuthors);
      setSummary(volume.summary);
      setSeries(volume.series);
      setImages(volume.images);
      setTags(volume.tags);
      setNotes(volume.notes);
   }

   @Override
   public void setVolumeNumber(String volumeNumber)
   {
      this.volume.volumeNumber = volumeNumber;
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      volume.authors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDV> titles)
   {
      volume.titles = new HashSet<>(titles);
   }

   @Override
   public void setOtherAuthors(List<AuthorRefDV> otherAuthors)
   {
      volume.otherAuthors = new ArrayList<>(otherAuthors);
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
      volume.images = new ArrayList<>(images);
   }

   @Override
   public void setTags(Collection<String> tags)
   {
      volume.tags = new HashSet<>(tags);
   }

   @Override
   public void setNotes(Collection<String> notes)
   {
      volume.notes = new HashSet<>(notes);
   }

}
