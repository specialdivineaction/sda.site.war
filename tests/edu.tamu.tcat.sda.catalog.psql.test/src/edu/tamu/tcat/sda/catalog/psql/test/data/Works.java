package edu.tamu.tcat.sda.catalog.psql.test.data;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.DateDescriptionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

public class Works
{
   public WorkDV workDV;
   private AuthorRefDV authorRef;
   private Set<TitleDV> titleSet;
   private DateDescriptionDV dateDescript;
   private PublicationInfoDV pubInfo;

   List<AuthorRefDV> authorList = new ArrayList<>();
   List<AuthorRefDV> otherAuthorList = new ArrayList<>();

   public Works()
   {
      workDV = new WorkDV();
      authorRef = new AuthorRefDV();
      titleSet = new HashSet<TitleDV>();
      dateDescript = new DateDescriptionDV();
      pubInfo = new PublicationInfoDV();
   }

   public WorkDV buildWork()
   {
      return build();
   }

   private WorkDV build()
   {
       authorRef.authorId = "1234";
       authorRef.name = "A.C. Dixon";
       authorRef.role = "Autor";
       authorList.add(authorRef);


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

      dateDescript = new DateDescriptionDV();
      dateDescript.display = "";
      dateDescript.value = new GregorianCalendar(1856, 1, 28).getTime();

      pubInfo = new PublicationInfoDV();
      pubInfo.date = dateDescript;
      pubInfo.place = "";
      pubInfo.publisher = "";

      workDV.authors = authorList;
      workDV.otherAuthors = otherAuthorList;
      workDV.titles = titleSet;
      workDV.pubInfo = pubInfo;
      workDV.series = "Series 1";
      workDV.summary = "Summary of the work";

      return workDV;
   }
}
