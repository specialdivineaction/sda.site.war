package edu.tamu.tcat.sda.catalog.psql;

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


   VolumeMutatorImpl(VolumeDV volume)
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
}
