package edu.tamu.tcat.sda.catalog.works.dv;

import edu.tamu.tcat.sda.catalog.works.AuthorReference;

public class AuthorRefDV
{
   public String authorId;
   public String name;
   public String role;

   public AuthorRefDV(AuthorReference author)
   {
      this.authorId = author.getId();
      this.name = author.getName();
      this.role = author.getRole();
   }

   public AuthorRefDV()
   {
   }
}
