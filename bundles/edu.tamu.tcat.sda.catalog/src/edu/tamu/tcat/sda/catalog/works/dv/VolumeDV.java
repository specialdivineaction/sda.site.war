package edu.tamu.tcat.sda.catalog.works.dv;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.sda.catalog.works.Volume;

public class VolumeDV
{
   public String id;
   public String volume;
   public List<AuthorRefDV> authors;
   public List<TitleDV> titles;
   public String summary;
   public String series;
   public List<URI> images;
   public Collection<String> tags;
   public Collection<String> notes;

   public VolumeDV(Volume vol)
   {
      id = vol.getId();

      volume = vol.getVolume();

      authors = vol.getAuthors().stream()
            .map((ref) -> new AuthorRefDV(ref))
            .collect(Collectors.toList());

      titles = vol.getTitles().stream()
            .map((title) -> new TitleDV(title))
            .collect(Collectors.toList());

      summary = vol.getSummary();

      series = vol.getSeries();

      images = vol.getImages();

      tags = vol.getTags();

      notes = vol.getNotes();
   }

   public VolumeDV()
   {
   }
}
