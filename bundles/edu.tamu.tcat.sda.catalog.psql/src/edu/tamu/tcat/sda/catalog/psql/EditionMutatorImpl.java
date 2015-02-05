package edu.tamu.tcat.sda.catalog.psql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import edu.tamu.tcat.sda.catalog.InvalidDataException;
import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.works.EditionMutator;
import edu.tamu.tcat.sda.catalog.works.VolumeMutator;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;

public class EditionMutatorImpl implements EditionMutator
{
   private final EditionDV edition;
   private Supplier<String> volumeIdSupplier;


   /**
    * @param edition
    * @param volumeIdSupplier Supplier to generate IDs for volumes.
    */
   EditionMutatorImpl(EditionDV edition, Supplier<String> volumeIdSupplier)
   {
      this.edition = edition;
      this.volumeIdSupplier = volumeIdSupplier;
   }


   @Override
   public String getId()
   {
      return edition.id;
   }


   @Override
   public void setAll(EditionDV edition)
   {
      setAuthors(edition.authors);
      setTitles(edition.titles);
      setOtherAuthors(edition.otherAuthors);
      setEditionName(edition.editionName);
      setPublicationInfo(edition.publicationInfo);
      setSummary(edition.summary);
      setSeries(edition.series);

      setVolumes(edition.volumes);
   }


   private void setVolumes(List<VolumeDV> volumes)
   {
      edition.volumes.clear();
      for (VolumeDV volume : volumes) {
         VolumeMutator mutator;

         try {
            mutator = (null == volume.id) ? createVolume() : editVolume(volume.id);
         }
         catch (NoSuchCatalogRecordException e) {
            throw new InvalidDataException("Failed to edit existing volume. A supplied volume contains an id [" + volume.id + "], "
                  + "but the identified volume cannot be retrieved for editing.", e);

         }

         mutator.setAll(volume);
      }
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      edition.authors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDV> titles)
   {
      edition.titles = new HashSet<>(titles);
   }

   @Override
   public void setOtherAuthors(List<AuthorRefDV> otherAuthors)
   {
      edition.otherAuthors = new ArrayList<>(otherAuthors);
   }

   @Override
   public void setEditionName(String editionName)
   {
      this.edition.editionName = editionName;
   }

   @Override
   public void setPublicationInfo(PublicationInfoDV pubInfo)
   {
      edition.publicationInfo = pubInfo;
   }

   @Override
   public void setSummary(String summary)
   {
      edition.summary = summary;
   }

   @Override
   public void setSeries(String series)
   {
      edition.series = series;
   }

   @Override
   public VolumeMutator createVolume()
   {
      VolumeDV volume = new VolumeDV();
      volume.id = volumeIdSupplier.get();
      edition.volumes.add(volume);
      return new VolumeMutatorImpl(volume);
   }

   @Override
   public VolumeMutator editVolume(String id) throws NoSuchCatalogRecordException
   {
      for (VolumeDV volume : edition.volumes) {
         if (volume.id.equals(id)) {
            return new VolumeMutatorImpl(volume);
         }
      }
      throw new NoSuchCatalogRecordException("Unable to find volume with id [" + id + "].");
   }

}
