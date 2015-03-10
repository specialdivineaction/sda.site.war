package edu.tamu.tcat.trc.entries.bib.copy.legacy;

import java.util.Collection;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.trc.entries.bib.Edition;
import edu.tamu.tcat.trc.entries.bib.Volume;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.dto.EditionDV;
import edu.tamu.tcat.trc.entries.bib.dto.VolumeDV;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;

/**
 *  This class provides the abilitiy to link a {@link DigitalContentProvider} and their digital content to a
 *  {@link Work},{@link Volume} or {@link Edition}.
 */
public interface DigitalCopyLinkRepository
{
    void linkWork(WorkDV work, Collection<Record> records);
    void linkVolume(VolumeDV work, Collection<Record> records);
    void linkEdition(EditionDV work, Collection<Record> records);
}
