package edu.tamu.tcat.sda.catalog.rest.graph.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.rest.graph.GraphDTO;
import edu.tamu.tcat.sda.catalog.rest.graph.pagerank.PageRank;
import edu.tamu.tcat.sda.catalog.rest.graph.pagerank.PageRankIterative;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;

public class PeopleGraphResource
{
   private final PeopleRepository peopleRepo;
   private final WorkRepository workRepo;
   private final RelationshipRepository relnRepo;


   public PeopleGraphResource(PeopleRepository peopleRepo, WorkRepository workRepo, RelationshipRepository relnRepo)
   {
      this.peopleRepo = peopleRepo;
      this.workRepo = workRepo;
      this.relnRepo = relnRepo;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public GraphDTO.SingleGraph getGraph()
   {
      Iterable<Person> people = () -> peopleRepo.listAll();

      GraphDTO.Graph graph = new GraphDTO.Graph();

      graph.nodes = StreamSupport.stream(people.spliterator(), true)
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      Set<String> nodeIds = graph.nodes.stream()
            .map(node -> node.id)
            .collect(Collectors.toSet());

      Iterable<Relationship> relationships = () -> relnRepo.getAllRelationships();

      List<GraphDTO.Edge> edges = StreamSupport.stream(relationships.spliterator(), true)
            .flatMap(reln -> RepoAdapter.toDTO(reln).stream())
            .flatMap(this::expandByAuthor)
            .filter(edge -> nodeIds.contains(edge.source) && nodeIds.contains(edge.target))
            .collect(Collectors.toList());

      graph.edges = combineEdges(edges);

      PageRank pageRank = new PageRankIterative(graph);
      pageRank.execute();

      return GraphDTO.SingleGraph.create(graph);
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
               edge.metadata.put("multiplicity", bucket.size());
               edge.id = null;

               return edge;
            })
            .collect(Collectors.toList());
   }

   /**
    * Spread a work-referencing edge into a stream of author-referencing edges.
    * @param workEdge
    * @return
    */
   private Stream<GraphDTO.Edge> expandByAuthor(GraphDTO.Edge workEdge)
   {
      Work sourceWork = workRepo.getWork(workEdge.source);
      Work targetWork = workRepo.getWork(workEdge.target);

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
