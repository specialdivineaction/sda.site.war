package edu.tamu.tcat.sda.catalog.works;

import java.util.Collection;
import java.util.List;

import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;

/**
 * Used to edit the properties of a {@link Volume}. A {@code VolumeMutator} is created
 * within the transactional scope of an {@link EditWorkCommand} via either the
 * {@link EditionMutator#createVolume()} or the {@link EditionMutator#editVolume(String)}
 * method. Changes made to the {@code Volume} modified by this mutator will take effect
 * when the parent {@link EditWorkCommand#execute()} method is invoked. Note that any changes
 * made after this commands {@code execute()} method is called will have indeterminate affects.
 * Note that implementations are typically not threadsafe.
 */
public interface VolumeMutator
{
   void setAll(VolumeDV volume);

   void setVolumeNumber(String volumeNumber);
   void setAuthors(List<AuthorRefDV> authors);
   void setTitles(Collection<TitleDV> titles);
   void setOtherAuthors(List<AuthorRefDV> otherAuthors);
   void setSeries(String series);

   void setSummary(String summary);

   /**
   *
   * @return The unique identifier for the volume that this mutator modifies.
   *         Will not be {@code null}. For newly created volumes, this identifier
   *         will be assigned when the java object is first created rather than when
   *         the volume is committed to the persistence layer.
   */
  String getId();
}
