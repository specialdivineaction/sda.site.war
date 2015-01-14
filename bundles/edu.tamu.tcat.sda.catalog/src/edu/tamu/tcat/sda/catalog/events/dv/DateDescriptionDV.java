package edu.tamu.tcat.sda.catalog.events.dv;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import edu.tamu.tcat.sda.catalog.events.DateDescription;

/**
 * A simple representation of historical date information that includes both a calendar
 * data (a Java {@link Instant}) and a description of that date.
 */
public class DateDescriptionDV
{
   public static java.time.format.DateTimeFormatter Iso8601Formatter = DateTimeFormatter.ISO_LOCAL_DATE;

   /** ISO 8601 local (YYYY-MM-DD) representation of this date. */
   public String calendar;

   /** A human readable description of this date. */
   public String description;     // NOTE use this to capture intended degree of precision

   public DateDescriptionDV()
   {
   }

   public DateDescriptionDV(DateDescription date)
   {
      LocalDate d = date.getCalendar();
      if (d != null)
      {
         this.calendar = Iso8601Formatter.format(d);
      }

      // TODO convert legacy eventDate into DateDescriptionDV and set to null
      this.description = date.getDescription();
   }

   public static DateDescription convert(DateDescriptionDV dv)
   {
      throw new UnsupportedOperationException();
   }

   private final class DateDescriptionImpl implements DateDescription
   {
      private final String display;
      private final LocalDate value;

      DateDescriptionImpl(DateDescriptionDV dv)
      {
         this.display = dv.description;
         this.value = (dv.calendar != null && !dv.calendar.trim().isEmpty())
                  ? LocalDate.parse(dv.calendar, Iso8601Formatter) : null;

      }
      @Override
      public String getDescription()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public LocalDate getCalendar()
      {
         return value;
      }
   }
}
