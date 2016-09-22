package edu.tamu.tcat.sda.catalog.rest.graph.v1;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.rest.graph.GraphDTO;
import edu.tamu.tcat.sda.catalog.rest.graph.pagerank.PageRank;
import edu.tamu.tcat.sda.catalog.rest.graph.pagerank.PageRankIterative;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;

public class BiblioGraphResource
{
   private final BibliographicEntryRepository workRepo;
   private final RelationshipRepository relnRepo;

   public BiblioGraphResource(BibliographicEntryRepository workRepo, RelationshipRepository relnRepo)
   {
      this.workRepo = workRepo;
      this.relnRepo = relnRepo;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public GraphDTO.SingleGraph getGraph()
   {
      // TODO allow filtering by work, relationship type, relationship direction, etc.

      Iterable<BibliographicEntry> works = () -> workRepo.listAll();

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

      PageRank pageRank = new PageRankIterative(graph, 0.75);
      pageRank.execute();

      return GraphDTO.SingleGraph.create(graph);
   }
}
