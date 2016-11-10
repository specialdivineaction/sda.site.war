package edu.tamu.tcat.sda.rest.graph.v1;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.rest.graph.GraphDTO;
import edu.tamu.tcat.sda.rest.graph.GraphDTO.SingleGraph;
import edu.tamu.tcat.sda.rest.graph.pagerank.PageRank;
import edu.tamu.tcat.sda.rest.graph.pagerank.PageRankIterative;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class BiblioGraphResource
{

   private static final AtomicReference<GraphDTO.SingleGraph> graph = new AtomicReference<>();

   private final BibliographicEntryRepository workRepo;
   private final RelationshipRepository relnRepo;
   private final EntryResolverRegistry resolvers;

   public BiblioGraphResource(BibliographicEntryRepository workRepo, RelationshipRepository relnRepo, EntryResolverRegistry resolvers)
   {
      this.workRepo = workRepo;
      this.relnRepo = relnRepo;
      this.resolvers = resolvers;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public GraphDTO.SingleGraph getGraph()
   {
   // HACK need to listen to relationships and rebuild if required.
      SingleGraph g = graph.get();
      if (g != null)
         return g;

      // TODO allow filtering by work, relationship type, relationship direction, etc.

      Iterable<BibliographicEntry> works = () -> workRepo.listAll();

      GraphDTO.Graph dto = new GraphDTO.Graph();

      dto.type = "work-reln";
      dto.nodes = StreamSupport.stream(works.spliterator(), true)
         .map(work -> RepoAdapter.toDTO(work, resolvers))
         .collect(Collectors.toList());

      Set<String> nodeIds = dto.nodes.stream()
         .map(node -> node.id)
         .collect(Collectors.toSet());

      Iterable<Relationship> relationships = () -> relnRepo.listAll();

      dto.edges = StreamSupport.stream(relationships.spliterator(), true)
         .flatMap(reln -> RepoAdapter.toDTO(reln, resolvers).stream())
         .filter(edge -> nodeIds.contains(edge.source) && nodeIds.contains(edge.target))
         .collect(Collectors.toList());

      PageRank pageRank = new PageRankIterative(dto, 0.75);
      pageRank.execute();

      g = GraphDTO.SingleGraph.create(dto);

      graph.compareAndSet(null, g);
      return g;
   }
}
