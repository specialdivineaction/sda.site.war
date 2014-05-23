package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.tcat.sda.catalog.works.AuthorList;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDv;

public class AuthorListImpl implements AuthorList
{
   private final Iterator<AuthorRefDv> authRefDv;
   private List<AuthorReference> listHfDV;
   public AuthorListImpl(Iterator<AuthorRefDv> authRef)
   {
      this.authRefDv = authRef;
   }

   @Override
   public Iterator<AuthorReference> iterator()
   {
      listHfDV = new ArrayList<AuthorReference>();
      while(authRefDv.hasNext())
      {
         listHfDV.add(new AuthorReferenceImpl(authRefDv.next()));
      }
      return listHfDV.iterator();
   }

   @Override
   public AuthorReference get(int ix) throws IndexOutOfBoundsException
   {
      return listHfDV.get(ix);
   }

   @Override
   public int size()
   {
      return listHfDV.size();
   }

}
