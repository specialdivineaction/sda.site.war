package edu.tamu.tcat.sda.catalog.works;

import edu.tamu.tcat.sda.catalog.people.Person;

/**
 * Links the author of a work to a biographical record of the person. This allows the
 * bibliographic record to capture the name of the author as it appears on the work, along with
 * the role the author played in the creation of this work (e.g., author, translator, editor,
 * director, etc).
 *
 * <p>This can be understood as a join structure between bibliographic records and people
 * where the join record has additional information.
 *
 * @see WorkRepository#getAuthor(AuthorReference) To retrieve the {@link Person} associated
 * with an {@code AuthorReference}.
 */
public interface AuthorReference
{
   /**
    * @return The unique identifier of the referenced author.
    */
   String getId();

   /**
    * @return The name of the author as it appears on the work.
    */
   String getName();

   /**
    * @return The role this person played in the creation of the work, for example, author,
    *    translator, editor, director, etc.. This is an application specific value.
    */
   String getRole();
}
