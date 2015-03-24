package edu.tamu.tcat.trc.entries.bib.copy.postgres;

import edu.tamu.tcat.trc.entries.bib.copy.rest.v1.DigitalCopyLinkDTO;

public class PsqlDigitalCopyLinkTasksProvider implements PsqlDigitalCopyLinkTasks
{

   @Override
   public PsqlDigitalCopyCreateTask createDigitalCopyLink(DigitalCopyLinkDTO dcl)
   {
      return new PsqlDigitalCopyCreateTask(dcl);
   }

   @Override
   public PsqlDigitalCopyListTask listDigitalCopyLinks()
   {
      return new PsqlDigitalCopyListTask();
   }

   @Override
   public PsqlDigitalCopyListTask listDigitalCopyLinks(String bibliographyUrl)
   {
      return new PsqlDigitalCopyListTask(bibliographyUrl);
   }

}
