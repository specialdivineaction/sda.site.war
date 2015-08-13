package edu.tamu.tcat.sda.catalog.rest.export.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.function.Function;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class CsvExporter<SrcType, CsvType>
{
   private final Class<CsvType> recordType;
   private final Function<SrcType, CsvType> adapter;

   public CsvExporter(Function<SrcType, CsvType> adapter, Class<CsvType> recordType)
   {
      this.adapter = adapter;
      this.recordType = recordType;
   }

   public void export(Iterator<SrcType> iterator, Writer writer) throws IOException
   {
      CsvMapper mapper = new CsvMapper();
      CsvSchema schema = mapper.schemaFor(recordType);

      while (iterator.hasNext())
      {
         // HACK: replace with adapter support
         SrcType item = iterator.next();
         CsvType record = adapter.apply(item);

         mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
         String csv = mapper.writer(schema).writeValueAsString(record);
         writer.write(csv);
         writer.flush();
      }
   }
}
