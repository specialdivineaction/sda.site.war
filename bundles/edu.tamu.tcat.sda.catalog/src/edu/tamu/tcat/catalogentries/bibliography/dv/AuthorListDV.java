package edu.tamu.tcat.catalogentries.bibliography.dv;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.catalogentries.bibliography.AuthorList;
import edu.tamu.tcat.catalogentries.bibliography.AuthorReference;


public class AuthorListDV
{
   public int size;
   public List<AuthorRefDV> refs;

   public AuthorListDV(AuthorList authorList)
   {
      this.size = authorList.size();
      this.refs = new ArrayList<>();
      for (AuthorReference ref : authorList)
      {
         refs.add(new AuthorRefDV(ref));
      }
   }

   public AuthorListDV()
   {
   }
}
