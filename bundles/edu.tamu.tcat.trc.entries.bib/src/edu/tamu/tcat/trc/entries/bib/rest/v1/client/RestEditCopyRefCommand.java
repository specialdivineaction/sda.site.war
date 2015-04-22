package edu.tamu.tcat.trc.entries.bib.rest.v1.client;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.CopyRefDTO;
import edu.tamu.tcat.trc.entries.bib.CopyReference;
import edu.tamu.tcat.trc.entries.bib.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.entries.bib.UpdateCanceledException;
import edu.tamu.tcat.trc.entries.bib.postgres.PsqlDigitalCopyLinkRepo.UpdateEventFactory;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;

public class RestEditCopyRefCommand implements EditCopyReferenceCommand
{

   private final CopyReference original;
   private CopyRefDTO dto;
   private final AtomicBoolean executed = new AtomicBoolean(false);
   private EntryUpdateHelper<CopyReference> notifier;

   /**
    * Edit an existing copy.
    *
    * @param sqlExecutor
    * @param notifier
    * @param factory
    * @param dto
    */
   public RestEditCopyRefCommand(SqlExecutor sqlExecutor,
                                 EntryUpdateHelper<CopyReference> notifier,
                                 UpdateEventFactory factory,
                                 CopyRefDTO dto)
   {


      this.notifier = notifier;
      this.original = CopyRefDTO.instantiate(dto);
      this.dto = dto;
   }

   @Override
   public CopyReference getCurrentState()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public UUID getId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditCopyReferenceCommand setAssociatedEntry(URI uri)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditCopyReferenceCommand setCopyId(String id)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditCopyReferenceCommand setTitle(String title)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditCopyReferenceCommand setSummary(String summary)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditCopyReferenceCommand setRights(String description)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Future<CopyReference> execute() throws UpdateCanceledException
   {
      // TODO Auto-generated method stub
      return null;
   }

}
