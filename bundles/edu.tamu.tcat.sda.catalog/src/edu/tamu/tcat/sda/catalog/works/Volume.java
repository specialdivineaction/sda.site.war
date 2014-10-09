package edu.tamu.tcat.sda.catalog.works;

import java.net.URI;
import java.util.Collection;
import java.util.List;

public interface Volume
{
   /**
    * @return A unique system identifier for this volume.
    */
   String getId();

   /**
    * The number or authoritative identifier for this volume.
    *
    * @return
    */
   String getVolume();

   // the following properties may vary independently from works

   // TODO: These methods are copied directly from Edition, and those in turn are based on the
   //       fields in Work. Should these be refactored into a one or more base interfaces?

   /**
    * Volumes have their own series of authors who may not contribute to the underlying {@link
    * Edition} or {@link Work}. This list should be disjoint with the list of authors of the
    * underlying Edition and Work.
    *
    * @return
    */
   List<AuthorReference> getAuthors();

   /**
    * Titles of volumes may vary independently from the original work title. The first title will be
    * considered the canonical title of the Volume.
    *
    * @return
    */
   List<Title> getTitles();

   /**
    * Descriptive summary about this volume.
    *
    * @return
    */
   String getSummary();

   /**
    * Series to which the works belongs
    *
    * @return
    */
   String getSeries();

   /**
    * URLs to images of the edition (e.g. cover page, title page)
    *
    * TODO: images will eventually need more info such as:
    *       title, description, URI, source, and rights info
    *
    * @return
    */
   List<URI> getImages();

   /**
    * Tags for this edition
    *
    * @return
    */
   Collection<String> getTags();

   /**
    * Notes on this edition:
    *
    * @return
    */
   Collection<String> getNotes();

   // TODO: represent references
}
