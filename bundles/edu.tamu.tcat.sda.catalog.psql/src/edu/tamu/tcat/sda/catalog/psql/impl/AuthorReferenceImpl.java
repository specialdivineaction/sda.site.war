package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;

public class AuthorReferenceImpl implements AuthorReference
{
   private final AuthorRefDV authorRef;

   @Override
   public String getId()
   {
      return authorRef.authorId;
   }
   
   public AuthorReferenceImpl(AuthorRefDV authorRef)
   {
      this.authorRef = authorRef;
   }
   
   @Override
   public HistoricalFigure getAuthor()
   {
      // TODO Auto-generated method stub
      return authorRef.histFigure;
   }

   @Override
   public String getName()
   {
      return authorRef.name;
   }

   @Override
   public String getRole()
   {
      return authorRef.role;
   }

}
