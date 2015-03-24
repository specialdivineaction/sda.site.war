package edu.tamu.tcat.trc.entries.bib.copy.postgres;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopyLink;
import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopyLinkRepository;
import edu.tamu.tcat.trc.entries.bib.copy.rest.v1.DigitalCopyLinkDTO;

public class PsqlDigitalCopyLinkRepo implements DigitalCopyLinkRepository
{
   private SqlExecutor exec;
   private PsqlDigitalCopyLinkTasksProvider taskProvider;

   public PsqlDigitalCopyLinkRepo()
   {
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void activate()
   {
      taskProvider = new PsqlDigitalCopyLinkTasksProvider();
   }

   public void dispose()
   {
      this.exec = null;
   }

   @Override
   public void create(DigitalCopyLinkDTO dcl)
   {
      PsqlDigitalCopyCreateTask task = taskProvider.createDigitalCopyLink(dcl);
      exec.submit(task);
   }

   @Override
   public Iterable<DigitalCopyLink> getLinks()
   {
      PsqlDigitalCopyListTask task = taskProvider.listDigitalCopyLinks();
      exec.submit(task);
      return null;
   }

   @Override
   public Iterable<DigitalCopyLink> getLinks(String bibliography)
   {
      PsqlDigitalCopyListTask task = taskProvider.listDigitalCopyLinks(bibliography);
      exec.submit(task);
      return null;
   }
}
