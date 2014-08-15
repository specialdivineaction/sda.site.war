package edu.tamu.tcat.sda.catalog.psql.tasks;

import java.util.Objects;

import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

/**
 * Provides access to database task implementations to support the {@link WorkRepository}.
 */
public class PsqlWorkDbTasksProvider
{
   // TODO This should be made to extend an interface. This class and the associated
   //      tasks will be contain all PostgreSQL specific functionality and allow the
   //      logic in the core repo impl to be independent of the underlying DB layer.

   private JsonMapper mapper;

   public PsqlWorkDbTasksProvider()
   {
      // TODO Auto-generated constructor stub
   }

   public void setJsonMapper(JsonMapper mapper)
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

   public PsqlGetWorkTask makeGetWorkTask(int id)
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
