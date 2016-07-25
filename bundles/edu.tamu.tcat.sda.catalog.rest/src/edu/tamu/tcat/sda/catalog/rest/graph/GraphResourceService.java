package edu.tamu.tcat.sda.catalog.rest.graph;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.sda.catalog.rest.graph.v1.PeopleGraphResource;
import edu.tamu.tcat.sda.catalog.rest.graph.v1.WorkGraphResource;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;

@Path("graph")
public class GraphResourceService
{
   private static final Logger logger = Logger.getLogger(GraphResourceService.class.getName());

   private PeopleRepository peopleRepo;
   private WorkRepository workRepo;
   private RelationshipRepository relnRepo;

   public void setPeopleRepository(PeopleRepository peopleRepo)
   {
      this.peopleRepo = peopleRepo;
   }

   public void setWorkRepository(WorkRepository workRepo)
   {
      this.workRepo = workRepo;
   }

   public void setRelationshipRepository(RelationshipRepository relnRepo)
   {
      this.relnRepo = relnRepo;
   }

   public void activate()
   {
      Objects.requireNonNull(peopleRepo, "People repository not provided");
      Objects.requireNonNull(workRepo, "Work repository not provided");
      Objects.requireNonNull(relnRepo, "Relationship repository not provided");
      logger.log(Level.INFO, "starting graph REST resource service");
   }

   public void dispose()
   {
      peopleRepo = null;
      workRepo = null;
      relnRepo = null;
   }

   @Path("works")
   public WorkGraphResource rollupWorks()
   {
      return new WorkGraphResource(workRepo, relnRepo);
   }

   @Path("people")
   public PeopleGraphResource rollupPeople()
   {
      return new PeopleGraphResource(peopleRepo, workRepo, relnRepo);
   }
}
