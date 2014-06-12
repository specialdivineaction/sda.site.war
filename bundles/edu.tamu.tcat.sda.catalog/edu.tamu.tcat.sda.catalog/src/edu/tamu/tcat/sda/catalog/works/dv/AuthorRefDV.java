package edu.tamu.tcat.sda.catalog.works.dv;

import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;

public class AuthorRefDV
{
   public String authorId;
   public String name;
   public String role;
   public HistoricalFigure histFigure;
   
   public AuthorRefDV(AuthorReference author)
   {
      this.authorId = author.getId();
      this.name = author.getName();
      this.role = author.getRole();
      this.histFigure = author.getAuthor();
   }
   
   public AuthorRefDV()
   {
   }
}
