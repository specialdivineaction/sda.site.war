package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.tcat.sda.catalog.works.AuthorList;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorListDV;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;

public class AuthorListImpl implements AuthorList
{
   private List<AuthorReference> authRef;

   public AuthorListImpl(AuthorListDV authList)
   {
      authRef = new ArrayList<AuthorReference>();
      for (AuthorRefDV ref : authList.refs)
      {
         authRef.add(new AuthorReferenceImpl(ref));
      }
   }

   @Override
   public Iterator<AuthorReference> iterator()
   {
      return authRef.iterator();
   }

   @Override
   public AuthorReference get(int ix) throws IndexOutOfBoundsException
   {
      return authRef.get(ix);
   }

   @Override
   public int size()
   {
      return authRef.size();
   }

}
