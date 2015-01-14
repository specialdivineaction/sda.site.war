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
   public String instant;

   /** A human readable description of this date. */
   public String display;     // NOTE use this to capture intended degree of precision

   public DateDescriptionDV()
   {
   }

   public DateDescriptionDV(DateDescription date)
   {
      LocalDate d = date.getDate();
      if (d != null)
      {
         this.instant = Iso8601Formatter.format(d);
      }

      this.display = date.getDisplay();
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
         this.display = dv.display;
         this.value = (dv.instant != null && !dv.instant.trim().isEmpty())
                  ? LocalDate.parse(dv.instant, Iso8601Formatter) : null;

      }
      @Override
      public String getDisplay()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public LocalDate getDate()
      {
         return value;
      }
   }
}
