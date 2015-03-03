package edu.tamu.tcat.catalogentries.bibliography.dv;

import edu.tamu.tcat.catalogentries.bibliography.AuthorReference;

public class AuthorRefDV
{
   public String authorId;
   public String name;
   public String firstName;
   public String lastName;
   public String role;

   public AuthorRefDV(AuthorReference author)
   {
      this.authorId = author.getId();
      this.name = author.getName();
      if (this.name != null)
         parseLegacyName();

      String fName = author.getFirstName();
      String lName = author.getLastName();

      this.firstName = ((fName != null) && !fName.trim().isEmpty()) ? fName : this.firstName;
      this.lastName = ((lName != null) && !lName.trim().isEmpty()) ? lName : this.lastName;

      this.role = author.getRole();
   }

   private void parseLegacyName()
   {
      // HACK for legacy entries, try to split out first and last names.
      // TODO remove once data in DB has been converted.
      this.name = this.name.trim();
      int ix = this.name.lastIndexOf(",");
      ix = ix > 0 ? ix : this.name.lastIndexOf(";");
      if (ix > 0)
      {
         this.firstName = name.substring(ix + 1).trim();
         this.lastName = name.substring(0, ix).trim();
      }

      ix = this.name.lastIndexOf(" ");
      if (ix > 0)
      {
         this.lastName = name.substring(ix + 1).trim();
         this.firstName = name.substring(0, ix).trim();

      }
   }

   public AuthorRefDV()
   {
   }
}
