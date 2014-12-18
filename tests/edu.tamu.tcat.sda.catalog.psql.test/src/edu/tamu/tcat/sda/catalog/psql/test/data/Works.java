package edu.tamu.tcat.sda.catalog.psql.test.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.DateDescriptionDV;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class Works
{
   public Works()
   {
   }

   public WorkDV addWork()
   {
      WorkDV workDV = new WorkDV();
      workDV.authors = addAuthors();
      workDV.otherAuthors = addAuthors();
      workDV.titles = addTitles();
      workDV.series = "Series 1";
      workDV.summary = "Summary of the work";
      return workDV;
   }

   public EditionDV addEdition()
   {
      EditionDV edition = new EditionDV();
      edition.editionName = "";
      edition.authors = addAuthors();
      edition.otherAuthors = addAuthors();
      edition.titles = addTitles();
      edition.publicationInfo = addPublication();
      edition.series = "";
      edition.summary = "";
      return edition;
   }

   public VolumeDV addVolume()
   {
      VolumeDV volume = new VolumeDV();
      volume.volumeNumber = "I";
      volume.authors = addAuthors();
      volume.otherAuthors = addAuthors();
      volume.titles = addTitles();
      volume.series = "Volume Series 1";
      volume.summary = "Volume summary";
      return volume;
   }

   Collection<String> addNotes()
   {
      Set<String> notes = new HashSet<>();
      notes.add("note one");
      notes.add("note two");
      notes.add("note three");
      notes.add("note four");
      return notes;
   }

   Collection<String> addTags()
   {
      Set<String> tags = new HashSet<>();
      tags.add("Tag one");
      tags.add("Tag two");
      return tags;
   }

   List<URI> addImages()
   {
      List<URI> images = new ArrayList<>();
      return images;
   }

   List<AuthorRefDV> addAuthors()
   {
      List<AuthorRefDV> authorList = new ArrayList<>();
      AuthorRefDV authorRef = new AuthorRefDV();
      authorRef.authorId = "1234";
      authorRef.name = "A.C. Dixon";
      authorRef.role = "Autor";
      authorList.add(authorRef);
      return authorList;
   }

   Collection<TitleDV> addTitles()
   {
      Set<TitleDV> titleSet = new HashSet<TitleDV>();
      TitleDV canonical = new TitleDV();
      canonical.title = "Canonical Full Title Name";
      canonical.subtitle = "With subtitle";
      canonical.lg = "EN";
      canonical.type = "canonical";

      // Alternative Titles
      TitleDV shortTitle = new TitleDV();
      shortTitle.title = "Short Title";
      shortTitle.subtitle = "";
      shortTitle.lg = "EN";
      shortTitle.type = "short";

      TitleDV localeTitle = new TitleDV();
      localeTitle.title = "Locale Nombre completo Titulo";
      localeTitle.subtitle = "";
      localeTitle.lg = "ES";
      localeTitle.type = "locale";

      titleSet.add(canonical);
      titleSet.add(shortTitle);
      titleSet.add(localeTitle);
      return titleSet;
   }

   PublicationInfoDV addPublication()
   {
      PublicationInfoDV pubInfo = new PublicationInfoDV();

      DateDescriptionDV dateDescript = new DateDescriptionDV();
      dateDescript.display = "";
      dateDescript.value = new GregorianCalendar(1856, 1, 28).getTime();

      pubInfo.date = dateDescript;
      pubInfo.place = "London";
      pubInfo.publisher = "J.C. Publishing";

      return pubInfo;
   }

}
