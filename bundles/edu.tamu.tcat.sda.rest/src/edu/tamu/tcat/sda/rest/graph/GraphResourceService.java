package edu.tamu.tcat.sda.rest.graph;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.sda.rest.graph.v1.BiblioGraphResource;
import edu.tamu.tcat.sda.rest.graph.v1.BioGraphResource;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

@Path("graph")
public class GraphResourceService
{
   private static final Logger logger = Logger.getLogger(GraphResourceService.class.getName());

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
         Objects.requireNonNull(repoRegistry, "repository registry not provided");
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to start graph REST API service", e);
         throw e;
      }
   }

   public void dispose()
   {
      repoRegistry = null;
   }

   @Path("works")
   public BiblioGraphResource rollupWorks()
   {
      BibliographicEntryRepository workRepo = repoRegistry.getRepository(null, BibliographicEntryRepository.class);
      RelationshipRepository relnRepo = repoRegistry.getRepository(null, RelationshipRepository.class);
      EntryResolverRegistry resolvers = repoRegistry.getResolverRegistry();
      return new BiblioGraphResource(workRepo, relnRepo, resolvers);
   }

   @Path("people")
   public BioGraphResource rollupPeople()
   {
      BiographicalEntryRepository peopleRepo = repoRegistry.getRepository(null, BiographicalEntryRepository.class);
      RelationshipRepository relnRepo = repoRegistry.getRepository(null, RelationshipRepository.class);
      EntryResolverRegistry resolvers = repoRegistry.getResolverRegistry();
      return new BioGraphResource(peopleRepo, relnRepo, resolvers);
   }
}
