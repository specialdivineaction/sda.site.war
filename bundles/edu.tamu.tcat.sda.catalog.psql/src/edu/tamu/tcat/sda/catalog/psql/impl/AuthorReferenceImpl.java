package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.catalogentries.bibliography.AuthorReference;
import edu.tamu.tcat.catalogentries.bibliography.dv.AuthorRefDV;

public class AuthorReferenceImpl implements AuthorReference
{
   private final String id;
   private final String name;
   private final String firstName;
   private final String lastName;
   private final String role;

   public AuthorReferenceImpl(AuthorRefDV authorRef)
   {
      this.id = authorRef.authorId;
      this.name = authorRef.name;
      this.lastName = authorRef.lastName;
      this.firstName = authorRef.firstName;
      this.role = authorRef.role;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public String getFirstName()
   {
      return firstName;
   }

   @Override
   public String getLastName()
   {
      return lastName;
   }

   @Override
   public String getRole()
   {
      return role;
   }
}
