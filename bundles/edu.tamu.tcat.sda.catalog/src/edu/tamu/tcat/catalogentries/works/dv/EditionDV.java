package edu.tamu.tcat.catalogentries.works.dv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.catalogentries.works.Edition;

public class EditionDV
{
   public String id;
   public String editionName;
   public PublicationInfoDV publicationInfo;
   public List<AuthorRefDV> authors;
   public Collection<TitleDV> titles;
   public List<AuthorRefDV> otherAuthors;
   public String summary;
   public String series;
   // Hack: Editions may not contain volumes by default and can be added later.
   public List<VolumeDV> volumes = new ArrayList<VolumeDV>();

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
