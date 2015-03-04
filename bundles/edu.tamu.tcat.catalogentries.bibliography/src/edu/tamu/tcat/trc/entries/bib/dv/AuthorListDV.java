package edu.tamu.tcat.trc.entries.bib.dv;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.bib.AuthorList;
import edu.tamu.tcat.trc.entries.bib.AuthorReference;


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
