package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.tcat.sda.catalog.works.AuthorList;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorListDV;

public class AuthorListImpl implements AuthorList
{
   private Iterator<AuthorListDV> authListDv;
   private List<AuthorReference> authRef;
   private AuthorListDV authList;
   
   public AuthorListImpl(Iterator<AuthorListDV> authList)
   {
      this.authListDv = authList;
      authRef = new ArrayList<AuthorReference>();
      while(authListDv.hasNext())
      {
         authRef.add(new AuthorReferenceImpl(authListDv.next().authorReference));
      }
   }
   
   public AuthorListImpl(AuthorListDV authList)
   {
      this.authListDv = authListDv;
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
