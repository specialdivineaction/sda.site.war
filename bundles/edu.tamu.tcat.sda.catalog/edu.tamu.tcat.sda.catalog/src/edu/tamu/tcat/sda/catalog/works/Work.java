package edu.tamu.tcat.sda.catalog.works;

import java.util.List;

public interface Work
{

   List<AuthorReference> getAuthors();
   
   Title getTitle();
   
   String getSeries();
   
   String getSummary();
   
   List<String> getNotes();
   
   
}
