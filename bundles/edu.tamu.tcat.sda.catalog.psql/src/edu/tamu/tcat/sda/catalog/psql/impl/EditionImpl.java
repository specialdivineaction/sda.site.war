package edu.tamu.tcat.sda.catalog.psql.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.Edition;
import edu.tamu.tcat.sda.catalog.works.PublicationInfo;
import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.Volume;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;

public class EditionImpl implements Edition
{
   private String id;
   private List<AuthorReference> authors;
   private List<Title> titles;
   private List<AuthorReference> otherAuthors;
   private String edition;
   private PublicationInfo publicationInfo;
   private List<Volume> volumes;
   private String series;
   private String summary;
   private List<URI> images;
   private Collection<String> tags;
   private Collection<String> notes;


   public EditionImpl()
   {
   }

   public EditionImpl(EditionDV dv)
   {
      id = dv.id;

      authors = dv.authors.stream()
            .map((a) -> new AuthorReferenceImpl(a))
            .collect(Collectors.toList());

      titles = dv.titles.stream()
            .map((t) -> new TitleImpl(t))
            .collect(Collectors.toList());

      otherAuthors = dv.otherAuthors.stream()
            .map((a) -> new AuthorReferenceImpl(a))
            .collect(Collectors.toList());

      edition = dv.edition;

      publicationInfo = new PublicationImpl(dv.publicationInfo);

      volumes = dv.volumes.stream()
            .map((v) -> new VolumeImpl(v))
            .collect(Collectors.toList());

      series = dv.series;

      summary = dv.summary;

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
   public List<AuthorReference> getOtherAuthors()
   {
      return otherAuthors;
   }

   @Override
   public String getEdition()
   {
      return edition;
   }

   @Override
   public PublicationInfo getPublicationInfo()
   {
      return publicationInfo;
   }

   @Override
   public List<Volume> getVolumes()
   {
      return volumes;
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
