package edu.tamu.tcat.trc.entries.bib.rest.v1.client;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.BaseEditCopyRefCmd;
import edu.tamu.tcat.trc.entries.bib.CopyRefDTO;
import edu.tamu.tcat.trc.entries.bib.CopyReference;
import edu.tamu.tcat.trc.entries.bib.UpdateCanceledException;
import edu.tamu.tcat.trc.entries.bib.postgres.PsqlDigitalCopyLinkRepo.UpdateEventFactory;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;

public class RestEditCopyRefCommand extends BaseEditCopyRefCmd
{

   private final CopyReference original;
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
      super(dto);

      this.notifier = notifier;
      this.original = CopyRefDTO.instantiate(dto);
   }



   @Override
   public Future<CopyReference> execute() throws UpdateCanceledException
   {
      throw new UnsupportedOperationException();
   }

}
