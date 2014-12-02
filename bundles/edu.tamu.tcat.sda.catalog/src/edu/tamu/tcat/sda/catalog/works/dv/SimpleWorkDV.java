package edu.tamu.tcat.sda.catalog.works.dv;

import java.util.ArrayList;

public class SimpleWorkDV
{
   public String id;
   public ArrayList<String> authorIds;
   public ArrayList<String> authorNames;
   public ArrayList<String> authorRole;
   public ArrayList<String> titleTypes;
   public ArrayList<String> lang;
   public ArrayList<String> titles;
   public ArrayList<String> subtitles;
//   public String publisher;
//   public String publisherLocation;
//   public String publishDateString;
//   public String publishDateValue;
   public String series;
   public String summary;

   public String _version_;

   public SimpleWorkDV()
   {}

   public SimpleWorkDV(WorkDV works)
   {
      this.id = works.id;
      for (AuthorRefDV author : works.authors)
      {
         authorIds.add(author.authorId);
         authorNames.add(author.name);
         authorRole.add(author.role);
      }

      for (TitleDV title : works.titles)
      {
         titles.add(title.title);
         subtitles.add(title.subtitle);
         lang.add(title.lg);
         titleTypes.add(title.type);
      }

//      PublicationInfoDV publication = works.pubInfo;
//      this.publisher = publication.publisher;
//      this.publisherLocation = publication.place;
//      this.publishDateString = publication.date.display;
//      this.publishDateValue = publication.date.value.toString();

      this.series = works.series;
      this.summary = works.summary;

      this._version_ = "";
   }
}
