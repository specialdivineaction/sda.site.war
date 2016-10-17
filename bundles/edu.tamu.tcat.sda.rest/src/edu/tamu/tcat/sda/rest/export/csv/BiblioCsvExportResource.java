package edu.tamu.tcat.sda.rest.export.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;

public class BiblioCsvExportResource
{
   private static final Logger logger = Logger.getLogger(BiblioCsvExportResource.class.getName());

   private static final List<String> csvHeaders = Arrays.asList(
         "type",
         "workId",
         "editionId",
         "volumeId",
         "editionName",
         "volumeNumber",
         "shortTitle",
         "canonicalTitle",
         "bibliographicTitle",
         "dateDisplay",
         "dateIso",
         "publisher",
         "place",
         "summary",
         "hasDigitalCopy",
         "authors");

   private final BibliographicEntryRepository workRepo;

   public BiblioCsvExportResource(BibliographicEntryRepository workRepo)
   {
      this.workRepo = workRepo;
   }

   private void doWrite(Writer writer) throws WebApplicationException
   {
      try
      {
         CsvExporter<WorkCsvRecord, WorkCsvRecord> exporter = new CsvExporter<>(o -> o, WorkCsvRecord.class);

         writer.write(String.join(", ", csvHeaders));
         writer.write(System.lineSeparator());

         Iterable<BibliographicEntry> iterable = () -> workRepo.listAll();
         Iterator<WorkCsvRecord> iterator = StreamSupport.stream(iterable.spliterator(), false)
            .flatMap(this::stream)
            .iterator();

         exporter.export(iterator, writer);
         writer.flush();
      }
      catch (IOException e)
      {
         // NOTE: various clients may abort connection before file download completes, resulting in this error
         logger.log(Level.FINE, "Failed to export author list. Unable to read data from repository.", e);
         Response resp = Response.serverError()
               .entity("Cannot complete author list export. Likely caused by closed connection.")
               .build();
         throw new WebApplicationException(resp);
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to export author list. Unexpected error.", e);
         Response resp = Response.serverError()
               .entity("Cannot export author list. See server logs for details.")
               .build();
         throw new WebApplicationException(resp);
      }
   }

   @GET
   @Produces("text/csv; charset=UTF-8")
   public Response getBasicWorkListCSV()
   {
      StreamingOutput stream = os -> {
         Writer writer = new BufferedWriter(new OutputStreamWriter(os));

         // HACK: fork for background thread to do CSV export using Jackson b/c if it's done in
         //       Jersey/REST call stack, it will get the wrong version of Jackson via different
         //       OSGi class loaders. Fork forces Jackson classes to be loaded from this class's
         //       class loader instead of Jersey's.
         ExecutorService svc = Executors.newSingleThreadExecutor();
         Future<?> future = svc.submit(() -> doWrite(writer));
         try
         {
            future.get(5, TimeUnit.MINUTES);
         }
         catch (Exception e)
         {
            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
         }
         finally
         {
            svc.shutdown();
         }
      };

      return Response.ok(stream)
            .header("Content-Disposition", "inline; filename=\"works.csv\"")
            .build();
   }


   private Stream<WorkCsvRecord> stream(BibliographicEntry work)
   {
      WorkCsvRecord workRecord = createRecord(work);
      Stream<WorkCsvRecord> editionRecords = work.getEditions().stream()
            .flatMap(edition -> stream(edition, work.getId()));

      return Stream.concat(Stream.of(workRecord), editionRecords);
   }

   private Stream<WorkCsvRecord> stream(Edition edition, String workId)
   {
      WorkCsvRecord editionRecord = create(edition, workId);
      Stream<WorkCsvRecord> volumeRecords = edition.getVolumes().stream()
            .map(volume -> create(volume, edition.getId(), workId));

      return Stream.concat(Stream.of(editionRecord), volumeRecords);
   }

   private WorkCsvRecord createRecord(BibliographicEntry work)
   {
      WorkCsvRecord record = new WorkCsvRecord();
      record.type = "work";
      record.workId = work.getId();

      // HACK: user has entered bibliographic title in place of canonical title
      //       and canonical title in place of short title.
      TitleDefinition workTitle = work.getTitle();
      if (workTitle != null)
      {
         record.canonicalTitle = workTitle.get("short")
               .map(Title::getFullTitle)
               .orElse(null);

         record.bibliographicTitle = workTitle.get("canonical")
               .map(Title::getFullTitle)
               .orElse(null);
      }

      record.summary = work.getSummary();

      Set<CopyReference> copies = work.getCopyReferences();
      record.hasDigitalCopy = copies.isEmpty() ? "false" : "true";

      StringJoiner sj = new StringJoiner(";");
      StreamSupport.stream(work.getAuthors().spliterator(), false)
            .map(BiblioCsvExportResource::formatName)
            .forEach(sj::add);
      record.authors = sj.toString();

      return record;
   }

   private WorkCsvRecord create(Edition edition, String workId)
   {
      WorkCsvRecord record = new WorkCsvRecord();
      record.type = "edition";
      record.workId = workId;
      record.editionId = edition.getId();
      record.editionName = edition.getEditionName();

      PublicationInfo publicationInfo = edition.getPublicationInfo();
      if (publicationInfo != null)
      {
         record.publisher = publicationInfo.getPublisher();
         record.place = publicationInfo.getLocation();

         DateDescription publicationDate = publicationInfo.getPublicationDate();
         if (publicationDate != null)
         {
            record.dateDisplay = publicationDate.getDescription();

            LocalDate calendarDate = publicationDate.getCalendar();
            if (calendarDate != null)
            {
               record.dateIso = calendarDate.toString();
            }
         }
      }

      record.canonicalTitle = edition.getTitles().stream()
            .filter(title -> title.getType().equalsIgnoreCase("short"))
            .findFirst()
            .map(title -> title.getFullTitle())
            .orElse(null);

      record.bibliographicTitle = edition.getTitles().stream()
            .filter(title -> title.getType().equalsIgnoreCase("canonical"))
            .findFirst()
            .map(title -> title.getFullTitle())
            .orElse(null);

      record.summary = edition.getSummary();

      Set<CopyReference> copies = edition.getCopyReferences();
      record.hasDigitalCopy = copies.isEmpty() ? "false" : "true";

      StringJoiner sj = new StringJoiner(";");
      StreamSupport.stream(edition.getAuthors().spliterator(), false)
            .map(BiblioCsvExportResource::formatName)
            .forEach(sj::add);
      record.authors = sj.toString();

      return record;
   }

   private WorkCsvRecord create(Volume volume, String editionId, String workId)
   {
      WorkCsvRecord record = new WorkCsvRecord();
      record.type = "volume";
      record.workId = workId;
      record.editionId = editionId;
      record.volumeId = volume.getId();
      record.volumeNumber = volume.getVolumeNumber();

      PublicationInfo publicationInfo = volume.getPublicationInfo();
      if (publicationInfo != null)
      {
         record.publisher = publicationInfo.getPublisher();
         record.place = publicationInfo.getLocation();

         DateDescription publicationDate = publicationInfo.getPublicationDate();
         if (publicationDate != null)
         {
            record.dateDisplay = publicationDate.getDescription();

            LocalDate calendarDate = publicationDate.getCalendar();
            if (calendarDate != null)
            {
               record.dateIso = calendarDate.toString();
            }
         }
      }

      record.canonicalTitle = volume.getTitles().stream()
            .filter(title -> title.getType().equalsIgnoreCase("short"))
            .findFirst()
            .map(title -> title.getFullTitle())
            .orElse(null);

      record.bibliographicTitle = volume.getTitles().stream()
            .filter(title -> title.getType().equalsIgnoreCase("canonical"))
            .findFirst()
            .map(title -> title.getFullTitle())
            .orElse(null);

      record.summary = volume.getSummary();

      Set<CopyReference> copies = volume.getCopyReferences();
      record.hasDigitalCopy = copies.isEmpty() ? "false" : "true";

      StringJoiner sj = new StringJoiner(";");
      StreamSupport.stream(volume.getAuthors().spliterator(), false)
            .map(BiblioCsvExportResource::formatName)
            .forEach(sj::add);
      record.authors = sj.toString();

      return record;
   }

   /**
    * Utility to format the name of an author reference
    *
    * @param ref
    * @return formatted first and last names
    */
   private static String formatName(AuthorReference ref)
   {
      String firstName = ref.getFirstName();
      String lastName = ref.getLastName();

      StringJoiner joiner = new StringJoiner(", ");

      if (lastName != null && !lastName.trim().isEmpty())
      {
         joiner.add(lastName.trim());
      }

      if (firstName != null && !firstName.trim().isEmpty())
      {
         joiner.add(firstName.trim());
      }

      String result = joiner.toString();
      return result.isEmpty() ? "[unnamed]" : result;
   }

   @JsonPropertyOrder
   public class WorkCsvRecord
   {
      public String type;
      public String workId;
      public String editionId;
      public String volumeId;
      public String editionName;
      public String volumeNumber;
      public String shortTitle;
      public String canonicalTitle;
      public String bibliographicTitle;
      public String dateDisplay;
      public String dateIso;
      public String publisher;
      public String place;
      public String summary;
      public String hasDigitalCopy;
      public String authors;
   }
}
