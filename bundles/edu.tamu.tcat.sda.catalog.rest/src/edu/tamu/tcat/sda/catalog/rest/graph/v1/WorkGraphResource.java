package edu.tamu.tcat.sda.catalog.rest.graph.v1;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.rest.graph.GraphDTO;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipDirection;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchResult;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchService;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;

public class WorkGraphResource
{
   private final WorkRepository workRepo;
   private final RelationshipSearchService relnSearchService;
   private final RelationshipRepository relnRepo;

   public WorkGraphResource(WorkRepository workRepo, RelationshipSearchService relnSearchService, RelationshipRepository relnRepo)
   {
      this.workRepo = workRepo;
      this.relnSearchService = relnSearchService;
      this.relnRepo = relnRepo;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public GraphDTO.SingleGraph getGraph()
   {
      // TODO allow filtering by work, relationship type, relationship direction, etc.

      Iterable<Work> works = () -> workRepo.getAllWorks();

      GraphDTO.Graph graph = new GraphDTO.Graph();

      graph.type = "work-reln";
      graph.nodes = StreamSupport.stream(works.spliterator(), true)
         .map(RepoAdapter::toDTO)
         .collect(Collectors.toList());

      graph.edges = graph.nodes.stream()
         .flatMap(node -> findRelationships(node.id, RelationshipDirection.any))
         .flatMap(reln -> RepoAdapter.toDTO(reln).stream())
         .collect(Collectors.toList());

      return GraphDTO.SingleGraph.create(graph);
   }

   private Stream<Relationship> findRelationships(String workId, RelationshipDirection direction)
   {
      try
      {
         RelationshipQueryCommand command = relnSearchService.createQueryCommand();
         command.forEntity(URI.create("works/" + workId), direction);
         RelationshipSearchResult result = command.execute();
         List<RelnSearchProxy> relationships = result.get();
         return relationships.stream()
            .map(proxy -> proxy.id)
            .map(relnRepo::get)
            .filter(Objects::nonNull);
      }
      catch (SearchException e)
      {
         return Stream.empty();
      }
   }
}
