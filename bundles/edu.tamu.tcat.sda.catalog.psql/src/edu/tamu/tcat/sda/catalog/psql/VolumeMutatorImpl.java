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
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented.");
   }

   @Override
   public void setVolume(String volume)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented.");
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented.");
   }

   @Override
   public void setTitles(List<TitleDV> titles)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented.");
   }

   @Override
   public void setSummary(String summary)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented.");
   }

   @Override
   public void setSeries(String series)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented.");
   }

   @Override
   public void setImages(List<URI> images)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented.");
   }

   @Override
   public void setTags(Collection<String> tags)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented.");
   }

   @Override
   public void setNotes(Collection<String> notes)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented.");
   }

}
