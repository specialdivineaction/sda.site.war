package edu.tamu.tcat.sda.catalog.psql.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.Volume;
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;

public class VolumeImpl implements Volume
{
   private String id;
   private String volumeNumber;
   private List<AuthorReference> authors;
   private List<Title> titles;
   private String summary;
   private String series;
   private List<URI> images;
   private Collection<String> tags;
   private Collection<String> notes;


   public VolumeImpl()
   {
   }

   public VolumeImpl(VolumeDV dv)
   {
      id = dv.id;

      volumeNumber = dv.volumeNumber;

      authors = dv.authors.stream()
            .map((a) -> new AuthorReferenceImpl(a))
            .collect(Collectors.toList());

      titles = dv.titles.stream()
            .map((t) -> new TitleImpl(t))
            .collect(Collectors.toList());

      summary = dv.summary;

      series = dv.series;

      images = dv.images;

      tags = dv.tags;

      notes = dv.notes;
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
   public List<AuthorReference> getAuthors()
   {
      return authors;
   }

   @Override
   public List<Title> getTitles()
   {
      return titles;
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

   @Override
   public List<URI> getImages()
   {
      return images;
   }

   @Override
   public Collection<String> getTags()
   {
      return tags;
   }

   @Override
   public Collection<String> getNotes()
   {
      return notes;
   }

}
