package edu.tamu.tcat.sda.catalog.rest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.tamu.tcat.sda.catalog.rest.export.csv.CsvExporter;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;

@Path("/export/authors")
public class AuthorList
{
   private static final Logger logger = Logger.getLogger(AuthorList.class.getName());

   private PeopleRepository repo;

   private List<String> csvHeaders;

   // called by DS
   public void setRepository(PeopleRepository repo)
   {
      this.repo = repo;
   }

   // called by DS
   public void activate()
   {
      csvHeaders = Arrays.asList(
                        "id",
                        "Display Name",
                        "Family Name",
                        "Given Name",
                        "Middle Name",
                        "Title",
                        "Suffix",
                        "Birth Place",
                        "Birth Date",
                        "Birth Date Label",
                        "Death Place",
                        "Death Date",
                        "Death Date Lable",
                        "remove");
   }

   // called by DS
   public void dispose()
   {

   }


   @GET
   @Produces("text/csv; charset=UTF-8")
   public Response getBasicAuthorListCSV() throws CatalogRepoException
   {
      CsvExporter<Person, PersonCsvRecord> exporter =
            new CsvExporter<>(PersonCsvRecord::create, PersonCsvRecord.class);

      StreamingOutput stream = new StreamingOutput() {
         @Override
         public void write(OutputStream os) throws WebApplicationException {
             Writer writer = new BufferedWriter(new OutputStreamWriter(os));
             try
             {
                writer.write(String.join(", ", csvHeaders));
                writer.write(System.lineSeparator());
                Iterator<Person> iterator = repo.listAll();
                exporter.export(iterator, writer);
                writer.flush();
             }
             catch (IOException ex)
             {
                // NOTE: various clients may abort a connection before the file download completes
                //       resulting in this error.
                logger.log(Level.FINE, "Failed to export author list. Unable to read data from repository.", ex);
                Response resp = Response.serverError()
                      .entity("Cannot complete author list export. Likely caused by closed connection.")
                      .build();
                throw new WebApplicationException(resp);
             }
             catch (CatalogRepoException ex)
             {
                logger.log(Level.SEVERE, "Failed to export author list. Unable to read data from repository.", ex);
                Response resp = Response.serverError()
                                        .entity("Cannot export author list. See server logs for details.")
                                        .build();
                throw new WebApplicationException(resp);
             }
             catch (Exception ex)
             {
                logger.log(Level.SEVERE, "Failed to export author list. Unexpected error.", ex);
                Response resp = Response.serverError()
                                        .entity("Cannot export author list. See server logs for details.")
                                        .build();
                throw new WebApplicationException(resp);
             }
         }
     };

     Response response = Response.ok(stream).build();
     response.getHeaders().add("Content-Disposition", "inline; filename=\"authors.csv\"");
     return response;
   }

   @JsonPropertyOrder
   public static class PersonCsvRecord
   {
      private static PersonCsvRecord create(Person person)
      {
         PersonCsvRecord record = new PersonCsvRecord();
         record.id = person.getId();
         PersonName canonicalName = person.getCanonicalName();
         if (canonicalName != null)
         {
            record.displayName = canonicalName.getDisplayName();
            record.familyName = canonicalName.getFamilyName();
            record.givenName = canonicalName.getGivenName();
            record.middleName = canonicalName.getMiddleName();
            record.title = canonicalName.getTitle();
            record.suffix = canonicalName.getSuffix();
         }

         HistoricalEvent birth = person.getBirth();
         if (birth != null)
         {
            record.birthPlace = birth.getLocation();
            LocalDate date = birth.getDate().getCalendar();
            if (date != null)
            {
               record.birthDate = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
            }
            record.birthDateLable = birth.getDate().getDescription();
         }

         HistoricalEvent death = person.getDeath();
         if (death != null)
         {
            record.deathPlace = death.getLocation();
            LocalDate date = death.getDate().getCalendar();
            if (date != null)
            {
               record.deathDate = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
            }
            record.deathDateLable = death.getDate().getDescription();
         }

         return record;
      }

      public String id;
      public String displayName;
      public String familyName;
      public String givenName;
      public String middleName;
      public String title;
      public String suffix;

      public String birthPlace;
      public String birthDate;
      public String birthDateLable;

      public String deathPlace;
      public String deathDate;
      public String deathDateLable;

      public boolean remove = false;
   }
}
