package edu.tamu.tcat.sda.catalog.works.dv;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.sda.catalog.works.Edition;

public class EditionDV
{
   public String id;
   public String editionName;
   public PublicationInfoDV publicationInfo;
   public List<VolumeDV> volumes;
   public List<AuthorRefDV> authors;
   public Collection<TitleDV> titles;
   public List<AuthorRefDV> otherAuthors;
   public String summary;
   public String series;

   public EditionDV(Edition ed)
   {
      id = ed.getId();

      editionName = ed.getEditionName();

      publicationInfo = new PublicationInfoDV(ed.getPublicationInfo());

      volumes = ed.getVolumes().stream()
            .map((vol) -> new VolumeDV(vol))
            .collect(Collectors.toList());

      authors = ed.getAuthors().stream()
            .map((ref) -> new AuthorRefDV(ref))
            .collect(Collectors.toList());

      titles = ed.getTitles().parallelStream()
            .map((title) -> new TitleDV(title))
            .collect(Collectors.toSet());

      otherAuthors = ed.getOtherAuthors().stream()
            .map((ref) -> new AuthorRefDV(ref))
            .collect(Collectors.toList());

      summary = ed.getSummary();

      series = ed.getSeries();
   }

   public EditionDV()
   {
   }
}
