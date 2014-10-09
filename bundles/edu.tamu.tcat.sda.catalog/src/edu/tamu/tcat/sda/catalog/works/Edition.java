package edu.tamu.tcat.sda.catalog.works;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * Editions are published manifestations of a {@link Work}.
 */
public interface Edition
{
   /**
    * @return A unique system identifier for this edition.
    */
   String getId();


   // the following properties may vary independently from works

   /**
    * The authors or other individuals responsible for the creation of this work. Note that different
    * editions of the same work may have different authors, for example, if an author joined or left
    * a multi-authored work.
    *
    * @return The authors of this edition.
    */
   List<AuthorReference> getAuthors();

   // the following properties may vary independently from works

   /**
    * The title(s) of this edition. Note that an individual edition may have different titles when,
    * for example, it is referenced using a shorter version of the full title; a title's spelling is
    * normalized or a title is translated. An edition may have a different title from the work it
    * is associated with.
    *
    * @return The titles associated with this edition.
    */
   List<Title> getTitles();

   /**
    * Other individuals who played a role in the creation of this work, but who are not primarily
    * responsible for its creation. Translators are a common example.
    *
    * @return The other authors associated with this work.
    */
   List<AuthorReference> getOtherAuthors();

   /**
    * The name or other identifier for this edition of the work. Editions of a work are frequently represented
    * as ordinals (2<sup>nd</sup>, 1<sup>st</sup>) but may use other conventions (e.g. 12 vol. ed; 1910-1915 edition;
    * anniversary edition). This value may be omitted if there is only one edition of the Work.
    *
    * @return The identifier for this edition of the work or {@code null} if no edition identifier is supplied.
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
    * @return An editorial summary of this edition. Typically 150 to 300 words.
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
    * @deprecated We may supply a different mechanism for creating images
    */
   @Deprecated
   List<URI> getImages();

   /**
    * Tags for this edition
    *
    * @return
    * @deprecated We may supply a different mechanism for working with tags. Notably, we'll use a
    *       concrete type for tags.
    */
   @Deprecated
   Collection<String> getTags();

   /**
    * Notes on this edition:
    *
    * @return
    * @deprecated We may supply a different mechanism for working with tags. Notably, we'll use a
    *       concrete type for notes.
    */
   @Deprecated
   Collection<String> getNotes();

   // TODO: represent references
}