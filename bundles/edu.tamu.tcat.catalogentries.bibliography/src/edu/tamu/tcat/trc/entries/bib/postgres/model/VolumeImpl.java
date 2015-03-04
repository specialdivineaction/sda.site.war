package edu.tamu.tcat.trc.entries.bib.postgres.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.bib.AuthorReference;
import edu.tamu.tcat.trc.entries.bib.PublicationInfo;
import edu.tamu.tcat.trc.entries.bib.Title;
import edu.tamu.tcat.trc.entries.bib.Volume;
import edu.tamu.tcat.trc.entries.bib.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.bib.dto.VolumeDV;

public class VolumeImpl implements Volume
{
   private String id;
   private String volumeNumber;
   private PublicationInfo publicationInfo;
   private List<AuthorReference> authors;
   private Collection<Title> titles;
   private List<AuthorReference> otherAuthors;
   private String summary;
   private String series;
//   private List<URI> images;
//   private Collection<String> tags;
//   private Collection<String> notes;


   public VolumeImpl()
   {
   }

   public VolumeImpl(VolumeDV dv)
   {
      id = dv.id;

      volumeNumber = dv.volumeNumber;

      publicationInfo = dv.publicationInfo == null ? new PublicationImpl(new PublicationInfoDV()) : new PublicationImpl(dv.publicationInfo);

      authors = dv.authors.stream()
            .map((a) -> new AuthorReferenceImpl(a))
            .collect(Collectors.toList());

      titles = dv.titles.parallelStream()
            .map((t) -> new TitleImpl(t))
            .collect(Collectors.toSet());

      otherAuthors = dv.otherAuthors.stream()
            .map((a) -> new AuthorReferenceImpl(a))
            .collect(Collectors.toList());

      summary = dv.summary;

      series = dv.series;
//
//      images = dv.images;
//
//      tags = dv.tags;
//
//      notes = dv.notes;
   }


   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getVolumeNumber()
   {
      return volumeNumber;
   }

   @Override
   public PublicationInfo getPublicationInfo()
   {
      return publicationInfo;
   }

   @Override
   public List<AuthorReference> getAuthors()
   {
      return authors;
   }

   @Override
   public Collection<Title> getTitles()
   {
      return titles;
   }

   @Override
   public List<AuthorReference> getOtherAuthors()
   {
      return otherAuthors;
   }

   @Override
   public String getSummary()
   {
      return summary;
   }

   @Override
   public String getSeries()
   {
      return series;
   }

//   @Override
//   public List<URI> getImages()
//   {
//      return images;
//   }
//
//   @Override
//   public Collection<String> getTags()
//   {
//      return tags;
//   }
//
//   @Override
//   public Collection<String> getNotes()
//   {
//      return notes;
//   }

}
