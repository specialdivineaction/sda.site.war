package edu.tamu.tcat.sda.catalog.works.dv;

import edu.tamu.tcat.sda.catalog.works.AuthorList;


public class AuthorListDV
{
   public int size;
   public AuthorRefDV authorReference;
   
   public AuthorListDV(AuthorList authorList)
   {
      this.size = authorList.size();
      this.authorReference = new AuthorRefDV(authorList.get(0));
   }
   
   public AuthorListDV()
   {
   }
}
