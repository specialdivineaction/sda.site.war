package edu.tamu.tcat.trc.entries.bib.copy.postgres;

import edu.tamu.tcat.trc.entries.bib.copy.rest.v1.DigitalCopyLinkDTO;

public interface PsqlDigitalCopyLinkTasks
{
   PsqlDigitalCopyCreateTask createDigitalCopyLink(DigitalCopyLinkDTO dcl);

   PsqlDigitalCopyListTask listDigitalCopyLinks();

   PsqlDigitalCopyListTask listDigitalCopyLinks(String bibliographyUrl);
}
