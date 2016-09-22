package edu.tamu.tcat.sda.catalog.rest.graph.v1;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.rest.graph.GraphDTO;
import edu.tamu.tcat.sda.catalog.rest.graph.pagerank.PageRank;
import edu.tamu.tcat.sda.catalog.rest.graph.pagerank.PageRankIterative;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;

public class BioGraphResource
{
   private static final Logger logger = Logger.getLogger(BioGraphResource.class.getName());

   private final BiographicalEntryRepository peopleRepo;
   private final BibliographicEntryRepository workRepo;
   private final RelationshipRepository relnRepo;


   public BioGraphResource(BiographicalEntryRepository peopleRepo, BibliographicEntryRepository workRepo, RelationshipRepository relnRepo)
   {
      this.peopleRepo = peopleRepo;
      this.workRepo = workRepo;
      this.relnRepo = relnRepo;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public GraphDTO.SingleGraph getGraph()
   {
      Iterable<BiographicalEntry> people = () -> peopleRepo.listAll();

      GraphDTO.Graph graph = new GraphDTO.Graph();

      graph.type = "people-reln";

      graph.nodes = StreamSupport.stream(people.spliterator(), true)
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      Set<String> nodeIds = graph.nodes.stream()
            .map(node -> node.id)
            .collect(Collectors.toSet());

      Iterable<Relationship> relationships = () -> relnRepo.getAllRelationships();

      List<GraphDTO.Edge> edges = StreamSupport.stream(relationships.spliterator(), true)
            .flatMap(this::relnToEdges)
            .flatMap(this::expandByAuthor)
            .filter(edge -> nodeIds.contains(edge.source) && nodeIds.contains(edge.target))
            .collect(Collectors.toList());

      graph.edges = combineEdges(edges);

      PageRank pageRank = new PageRankIterative(graph, 0.75);
      pageRank.execute();

      return GraphDTO.SingleGraph.create(graph);
   }

   private Stream<GraphDTO.Edge> relnToEdges(Relationship reln)
   {
      try
      {
         return RepoAdapter.toDTO(reln).stream();
      }
      catch (Exception e)
      {
         // skip any problematic relationships
         String msg = MessageFormat.format("Skipping conversion of relationship {0}: encountered an error during adaptation", reln.getId());
         logger.log(Level.WARNING, msg, e);
         return Stream.empty();
      }
   }

   /**
    * Spread a work-referencing edge into a stream of author-referencing edges.
    * @param workEdge
    * @return
    */
   private Stream<GraphDTO.Edge> expandByAuthor(GraphDTO.Edge workEdge)
   {
      BibliographicEntry sourceWork = workRepo.get(workEdge.source);
      BibliographicEntry targetWork = workRepo.get(workEdge.target);

      Collection<GraphDTO.Edge> authorEdges = new ArrayList<>();

      sourceWork.getAuthors().forEach(sourceRef -> {
         targetWork.getAuthors().forEach(targetRef -> {
            GraphDTO.Edge authorEdge = cloneEdge(workEdge);
            authorEdge.source = sourceRef.getId();
            authorEdge.target = targetRef.getId();

            authorEdges.add(authorEdge);
         });
      });

      return authorEdges.stream();
   }

   /**
    * Groups edges with identical source, target, and type into a single edge with a multiplicity parameter
    * @param edges
    * @return
    */
   private List<GraphDTO.Edge> combineEdges(List<GraphDTO.Edge> edges)
   {
      Map<String, Collection<GraphDTO.Edge>> buckets = new HashMap<>();

      edges.forEach(edge -> {
         String key = edge.source + ':' + edge.target + ':' + edge.relation;
         Collection<GraphDTO.Edge> bucket = buckets.computeIfAbsent(key, (k) -> new ArrayList<>());
         bucket.add(edge);
      });

      return buckets.values().stream()
            .map(bucket -> {
               if (bucket.isEmpty())
               {
                  throw new IllegalStateException("buckets should contain at least one entry");
               }

               // use any random entry as template for all
               GraphDTO.Edge edge = bucket.iterator().next();

               List<String> relationshipIds = bucket.stream()
                     .map(e -> e.id)
                     .collect(Collectors.toList());

               edge.metadata.clear();
               edge.metadata.put("relationshipIds", relationshipIds);
               edge.metadata.put("multiplicity", Integer.valueOf(bucket.size()));
               edge.id = null;

               return edge;
            })
            .collect(Collectors.toList());
   }

   private static GraphDTO.Edge cloneEdge(GraphDTO.Edge orig)
   {
      GraphDTO.Edge copy = new GraphDTO.Edge();
      copy.id = orig.id;
      copy.source = orig.source;
      copy.target = orig.target;
      copy.relation = orig.relation;
      copy.directed = orig.directed;
      copy.label = orig.label;
      copy.metadata = new HashMap<>(orig.metadata);

      return copy;
   }
}
