package edu.tamu.tcat.sda.catalog.rest.graph.v1;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.rest.graph.GraphDTO;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;

public class WorkGraphResource
{
   private final WorkRepository workRepo;
   private final RelationshipRepository relnRepo;

   public WorkGraphResource(WorkRepository workRepo, RelationshipRepository relnRepo)
   {
      this.workRepo = workRepo;
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

      Set<String> nodeIds = graph.nodes.stream()
         .map(node -> node.id)
         .collect(Collectors.toSet());

      Iterable<Relationship> relationships = () -> relnRepo.getAllRelationships();

      graph.edges = StreamSupport.stream(relationships.spliterator(), true)
         .flatMap(reln -> RepoAdapter.toDTO(reln).stream())
         .filter(edge -> nodeIds.contains(edge.source) && nodeIds.contains(edge.target))
         .collect(Collectors.toList());

      return GraphDTO.SingleGraph.create(graph);
   }
}
