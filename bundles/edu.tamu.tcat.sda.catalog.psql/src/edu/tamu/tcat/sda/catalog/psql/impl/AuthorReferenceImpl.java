package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;

public class AuthorReferenceImpl implements AuthorReference
{
   private final String id;
   private final String name;
   private final String role;

   public AuthorReferenceImpl(AuthorRefDV authorRef)
   {
      this.id = authorRef.authorId;
      this.name = authorRef.name;
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
   public String getRole()
   {
      return role;
   }
}
