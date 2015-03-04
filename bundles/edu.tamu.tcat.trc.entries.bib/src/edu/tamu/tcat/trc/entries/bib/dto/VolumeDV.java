package edu.tamu.tcat.trc.entries.bib.dto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.bib.Volume;

public class VolumeDV
{
   public String id;
   public String volumeNumber;
   public PublicationInfoDV publicationInfo;
   public List<AuthorRefDV> authors;
   public Collection<TitleDV> titles;
   public List<AuthorRefDV> otherAuthors;
   public String summary;
   public String series;
//   public List<URI> images;
//   public Collection<String> tags;
//   public Collection<String> notes;

   public VolumeDV(Volume vol)
   {
      id = vol.getId();

      volumeNumber = vol.getVolumeNumber();

      publicationInfo = new PublicationInfoDV(vol.getPublicationInfo());

      authors = vol.getAuthors().stream()
            .map((ref) -> new AuthorRefDV(ref))
            .collect(Collectors.toList());

      titles = vol.getTitles().parallelStream()
            .map((title) -> new TitleDV(title))
            .collect(Collectors.toSet());

      otherAuthors = vol.getOtherAuthors().stream()
            .map((ref) -> new AuthorRefDV(ref))
            .collect(Collectors.toList());

      summary = vol.getSummary();

      series = vol.getSeries();

//      images = vol.getImages();
//
//      tags = vol.getTags();
//
//      notes = vol.getNotes();
   }

   public VolumeDV()
   {
   }
}
