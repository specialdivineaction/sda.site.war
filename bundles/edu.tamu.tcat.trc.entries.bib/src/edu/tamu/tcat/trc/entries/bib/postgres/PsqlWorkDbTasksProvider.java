package edu.tamu.tcat.trc.entries.bib.postgres;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.bib.WorkRepository;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;

/**
 * Provides access to database task implementations to support the {@link WorkRepository}.
 */
public class PsqlWorkDbTasksProvider
{
   // TODO This should be made to extend an interface. This class and the associated
   //      tasks will be contain all PostgreSQL specific functionality and allow the
   //      logic in the core repo impl to be independent of the underlying DB layer.

   private ObjectMapper mapper;

   public PsqlWorkDbTasksProvider()
   {
      // TODO Auto-generated constructor stub
   }

   public void setJsonMapper(ObjectMapper mapper)
   {
      this.mapper = mapper;
   }

   public PsqlCreateWorkTask makeCreateWorkTask(WorkDV work)
   {
      Objects.requireNonNull(mapper, "No JSON mapper configured");

      return new PsqlCreateWorkTask(work, mapper);
   }

   public PsqlUpdateWorksTask makeUpdateWorksTask(WorkDV work)
   {
      Objects.requireNonNull(mapper, "No JSON mapper configured");

      return new PsqlUpdateWorksTask(work, mapper);
   }

   public PsqlListWorksTask makeListWorksTask()
   {
      Objects.requireNonNull(mapper, "No JSON mapper configured");

      return new PsqlListWorksTask(mapper);
   }

   public PsqlGetWorkTask makeGetWorkTask(String id)
   {
      Objects.requireNonNull(mapper, "No JSON mapper configured");

      return new PsqlGetWorkTask(id, mapper);
   }

   public <X> X getTask(Class<X> type, long timeout)
   {
      // TODO implement this.
      throw new UnsupportedOperationException();
   }
}
