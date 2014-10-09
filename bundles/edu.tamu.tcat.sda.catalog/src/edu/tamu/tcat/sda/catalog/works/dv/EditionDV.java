package edu.tamu.tcat.sda.catalog.works.dv;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.sda.catalog.works.Edition;

public class EditionDV
{
   public String id;
   public String edition;
   public PublicationInfoDV publicationInfo;
   public List<VolumeDV> volumes;
   public List<AuthorRefDV> authors;
   public List<TitleDV> titles;
   public List<AuthorRefDV> otherAuthors;
   public String summary;
   public String series;
   public List<URI> images;
   public Collection<String> tags;
   public Collection<String> notes;

   public EditionDV(Edition ed)
   {
      id = ed.getId();

      edition = ed.getEdition();

      publicationInfo = new PublicationInfoDV(ed.getPublicationInfo());

      volumes = ed.getVolumes().stream()
            .map((vol) -> new VolumeDV(vol))
            .collect(Collectors.toList());

      authors = ed.getAuthors().stream()
            .map((ref) -> new AuthorRefDV(ref))
            .collect(Collectors.toList());

      titles = ed.getTitles().stream()
            .map((title) -> new TitleDV(title))
            .collect(Collectors.toList());

      otherAuthors = ed.getOtherAuthors().stream()
            .map((ref) -> new AuthorRefDV(ref))
            .collect(Collectors.toList());

      summary = ed.getSummary();

      series = ed.getSeries();

      images = ed.getImages();

      tags = ed.getTags();

      notes = ed.getNotes();
   }

   public EditionDV()
   {
   }
}
