package edu.tamu.tcat.sda.catalog.psql.test.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class People
{
   private PersonDV histFig;
   private PersonNameDV author;
   private List<PersonDV> people;

   private static int person = 0;
   private static String[] givenName = {"Reuben", "Amzi"};
   private static String[] middleName = {"Archer", "Clarence"};
   private static String[] familyName = {"Torrey", "Dixon"};
   private static String[] displayName = {"R. A. Torrey", "A. C. Dixon"};
   private static String[] suffix = {"", ""};
   private static String[] title = {"Author", "Author"};
   private static String[] summary = {
               "Torrey was born in Hoboken, New Jersey, on 28 January 1856. He graduated from Yale University in 1875 and " +
               "Yale Divinity School in 1878. Following graduation, Torrey became a Congregational minister in Garrettsville, " +
               "Ohio, in 1878. The following year he married Clara Smith, and the Torreys had five children. After further " +
               "studies in theology at Leipzig University and Erlangen University in 1882�1883, Torrey joined Dwight L. Moody " +
               "in his evangelistic work in Chicago in 1889, and became superintendent of the Bible Institute of the Chicago " +
               "Evangelization Society (now Moody Bible Institute). Five years later, he became pastor of the Chicago Avenue " +
               "Church (now The Moody Church) in 1894.",
               "Amzi Clarence Dixon (July 6, 1854 � June 14, 1925) was a Baptist pastor, Bible expositor, and evangelist, " +
               "popular during the late 19th and early 20th centuries. With R.A. Torrey he edited an influential series of " +
               "essays, published as the The Fundamentals (1910-15), which gave fundamentalist Christianity its name. A. C. " +
               "Dixon was the brother of minister, playwright, and influential racist Thomas Dixon. Dixon was born on a " +
               "plantation near Shelby, North Carolina, on July 6, 1854 to a Baptist preacher. While still young, Dixon " +
               "believed he was called to preach the gospel."};
   private static LocalDate[] birthDate =  {LocalDate.of(1856, 1, 28), LocalDate.of(1854, 7, 6)};
   private static LocalDate[] deathDate =  {LocalDate.of(1928, 10, 26), LocalDate.of(1825, 6, 14)};

   public People()
   {
      GregorianCalendar gcalendar = new GregorianCalendar();
      author = new PersonNameDV();
      people = new ArrayList<>();
   }

   public PersonDV buildPerson()
   {
      return build(person);
   }

   public List<PersonDV> buildPeople(int numPeople)
   {
      for (int i = 0; i < numPeople; i++)
      {
         people.add(build(i));
      }
      return people;
   }

   private PersonDV build(int num)
   {
       histFig = new PersonDV();
       author.displayName = displayName[num];
       author.familyName = familyName[num];
       author.givenName = givenName[num];
       author.middleName = middleName[num];
       author.suffix = suffix[num];
       author.title = title[num];

       Set<PersonNameDV> authNames = new HashSet<PersonNameDV>();

       authNames.add(author);

       // histFig.id - is created by the DB and then set
       // TODO create a new DateOfDeath/Birth class
       histFig.birth = new HistoricalEventDV();
       histFig.birth.title = "Date of birth for " + author.displayName;
       histFig.birth.location = "England";
       histFig.birth.date = new DateDescriptionDV(null, birthDate[num]);

       histFig.death = new HistoricalEventDV();
       histFig.death.title = "Date of death for " + author.displayName;
       histFig.death.location = "England";
       histFig.death.date = new DateDescriptionDV(null, deathDate[num]);
       histFig.names = authNames;
       histFig.summary = summary[num];

       return histFig;
   }
}
