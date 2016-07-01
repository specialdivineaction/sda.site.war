package edu.tamu.tcat.sda.catalog.rest.graph;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.sda.catalog.rest.graph.v1.WorkGraphResource;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchService;

@Path("graph")
public class GraphResourceService
{
   private static final Logger logger = Logger.getLogger(GraphResourceService.class.getName());

   private WorkRepository workRepo;
   private RelationshipSearchService relnSearchService;
   private RelationshipRepository relnRepo;

   public void setWorkRepository(WorkRepository workRepo)
   {
      this.workRepo = workRepo;
   }

   public void setRelationshipSearchService(RelationshipSearchService relnSearchService)
   {
      this.relnSearchService = relnSearchService;
   }

   public void setRelationshipRepository(RelationshipRepository relnRepo)
   {
      this.relnRepo = relnRepo;
   }

   public void activate()
   {
      Objects.requireNonNull(workRepo, "Work repository not provided");
      Objects.requireNonNull(relnSearchService, "Relationship search service not provided");
      Objects.requireNonNull(relnRepo, "Relationship repository not provided");
      logger.log(Level.INFO, "starting graph REST resource service");
   }

   public void dispose()
   {
      workRepo = null;
      relnSearchService = null;
      relnRepo = null;
   }

   @Path("works")
   public WorkGraphResource rollupWorks()
   {
      return new WorkGraphResource(workRepo, relnSearchService, relnRepo);
   }
}
