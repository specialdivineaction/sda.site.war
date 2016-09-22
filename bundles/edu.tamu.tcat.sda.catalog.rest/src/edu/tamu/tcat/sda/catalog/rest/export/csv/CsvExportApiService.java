package edu.tamu.tcat.sda.catalog.rest.export.csv;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;

@Path("export")
public class CsvExportApiService
{
   private static final Logger logger = Logger.getLogger(CsvExportApiService.class.getName());

   private EntryRepositoryRegistry repoRegistry;

   public void setRepoRegistry(EntryRepositoryRegistry repoRegistry)
   {
      this.repoRegistry = repoRegistry;
   }

   public void activate()
   {
      try
      {
         logger.info(() -> "Activating " + getClass().getSimpleName());

         Objects.requireNonNull(repoRegistry, "no repository registry provided");
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to start CSV export API service", e);
      }
   }

   @Path("works")
   public BiblioCsvExportResource exportWorks()
   {
      BibliographicEntryRepository worksRepository = repoRegistry.getRepository(null, BibliographicEntryRepository.class);
      return new BiblioCsvExportResource(worksRepository);
   }

   @Path("people")
   public BioCsvExportResource exportPeople()
   {
      BiographicalEntryRepository peopleRepository = repoRegistry.getRepository(null, BiographicalEntryRepository.class);
      return new BioCsvExportResource(peopleRepository);
   }
}
