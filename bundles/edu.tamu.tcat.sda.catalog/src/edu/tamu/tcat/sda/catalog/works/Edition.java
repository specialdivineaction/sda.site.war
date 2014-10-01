package edu.tamu.tcat.sda.catalog.works;

import java.util.Collection;
import java.util.List;

/**
 * Editions are published manifestations of a {@link Work}.
 */
public interface Edition
{

   /**
    * Editions of a work are frequently represented as numbers in a bibliographic record, but could
    * use other conventions (e.g. years and year ranges). This value may be omitted if there is only
    * one edition of the Work.
    *
    * @return
    */
   String getEdition();

   /**
    * Editions are physical manifestations of {@link Work}s.
    *
    * @return
    */
   PublicationInfo getPublicationInfo();

   /**
    * Editions may consist of multiple {@link Volume}s.
    *
    * @return
    */
   List<Volume> getVolumes();


   // the following properties may vary independently from works

   /**
    * Editions have their own series of authors who may not contribute to the underlying {@link
    * Work}. For example, the author of a foreword. This list should be disjoint with the list of
    * authors of the underlying work.
    *
    * @return
    */
   List<AuthorReference> getAuthors();

   /**
    * Titles of published editions may vary independently from the original work title. The first
    * title will be considered the canonical title of the Edition.
    *
    * @return
    */
   List<Title> getTitles();

   /**
    * Descriptive summary about this edition.
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
    * @return
    */
   List<String> getImages();

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
