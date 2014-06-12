package edu.tamu.tcat.sda.catalog.works.dv;

import java.util.List;

/**
 * Represents a work 
 */
public class WorkDV
{
   public String id;
   public List<AuthorRefDv> authors;
   public List<TitleDV> titles;
   public List<AuthorRefDv> otherAuthors;
   public PublicationInfoDV pubInfo;
   public String summary;                    
}
