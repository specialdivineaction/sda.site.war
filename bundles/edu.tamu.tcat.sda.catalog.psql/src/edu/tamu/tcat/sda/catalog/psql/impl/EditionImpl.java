package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.catalogentries.bibliography.AuthorReference;
import edu.tamu.tcat.catalogentries.bibliography.Edition;
import edu.tamu.tcat.catalogentries.bibliography.PublicationInfo;
import edu.tamu.tcat.catalogentries.bibliography.Title;
import edu.tamu.tcat.catalogentries.bibliography.Volume;
import edu.tamu.tcat.catalogentries.bibliography.dv.EditionDV;

public class EditionImpl implements Edition
{
   private String id;

   private CommonFieldsDelegate delegate = new CommonFieldsDelegate();
   private String editionName;
   private PublicationInfo publicationInfo;
   private List<Volume> volumes;

   // TODO might belong in CommonFieldsDelegate?
   private String series;


   public EditionImpl()
   {
   }

   public EditionImpl(EditionDV dv)
   {
      id = dv.id;

      delegate = new CommonFieldsDelegate(dv.authors, dv.titles, dv.otherAuthors, dv.summary);

      editionName = dv.editionName;
      publicationInfo = new PublicationImpl(dv.publicationInfo);
      volumes = dv.volumes.stream()
            .map((v) -> new VolumeImpl(v))
            .collect(Collectors.toList());
      series = dv.series;
   }


   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public List<AuthorReference> getAuthors()
   {
      return delegate.getAuthors();
   }

   @Override
   public Collection<Title> getTitles()
   {
      return delegate.getTitles();
   }

   @Override
   public List<AuthorReference> getOtherAuthors()
   {
      return delegate.getOtherAuthors();
   }

   @Override
   public String getEditionName()
   {
      return editionName;
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
      return delegate.getSummary();
   }

   @Override
   public String getSeries()
   {
      return series;
   }

   @Override
   public Volume getVolume(String volumeId) throws NoSuchCatalogRecordException
   {
      for (Volume volume : volumes) {
         if (volume.getId().equals(volumeId)) {
            return volume;
         }
      }

      throw new NoSuchCatalogRecordException("Unable to find volume [" + volumeId + "] in edition [" + id + "].");
   }

}
