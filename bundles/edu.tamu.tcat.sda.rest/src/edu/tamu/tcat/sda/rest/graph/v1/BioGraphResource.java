package edu.tamu.tcat.sda.rest.graph.v1;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.rest.graph.GraphDTO;
import edu.tamu.tcat.sda.rest.graph.GraphDTO.Edge;
import edu.tamu.tcat.sda.rest.graph.pagerank.PageRank;
import edu.tamu.tcat.sda.rest.graph.pagerank.PageRankIterative;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class BioGraphResource
{
   private static final Logger logger = Logger.getLogger(BioGraphResource.class.getName());

   private final BiographicalEntryRepository peopleRepo;
   private final RelationshipRepository relnRepo;
   private final EntryResolverRegistry resolvers;


   public BioGraphResource(BiographicalEntryRepository peopleRepo, RelationshipRepository relnRepo, EntryResolverRegistry resolvers)
   {
      this.peopleRepo = peopleRepo;
      this.relnRepo = relnRepo;
      this.resolvers = resolvers;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public GraphDTO.SingleGraph getGraph()
   {

      GraphDTO.Graph graph = new GraphDTO.Graph();

      graph.type = "people-reln";
      graph.nodes = getPersonNodes();
      graph.edges = constructEdges(graph.nodes);

      // annotates all nodes
      PageRank pageRank = new PageRankIterative(graph, 0.75);
      pageRank.execute();

      return GraphDTO.SingleGraph.create(graph);
   }

   private List<GraphDTO.Node> getPersonNodes()
   {
      Iterable<BiographicalEntry> people = () -> peopleRepo.listAll();
      return StreamSupport.stream(people.spliterator(), true)
                  .map(person -> RepoAdapter.toDTO(person, resolvers))
                  .collect(Collectors.toList());
   }

   private List<GraphDTO.Edge> constructEdges(List<GraphDTO.Node> nodes)
   {
      Set<String> nodeIds = nodes.stream()
            .map(node -> node.id)
            .collect(Collectors.toSet());

      Iterable<Relationship> relationships = () -> relnRepo.listAll();

      List<GraphDTO.Edge> edges = StreamSupport.stream(relationships.spliterator(), true)
            .flatMap(this::relnToEdges)
            .filter(edge -> nodeIds.contains(edge.source) && nodeIds.contains(edge.target))
            .collect(Collectors.toList());

      return combineEdges(edges);
   }

   /**
    * Converts the given work-to-work relationship into a stream of person-to-person graph edges.
    * @param reln
    * @return
    */
   private Stream<GraphDTO.Edge> relnToEdges(Relationship reln)
   {
      try
      {
         RelationshipType type = reln.getType();

         Collection<String> relatedIds = expandByAuthor(reln.getRelatedEntities())
               .map(person -> resolvers.getResolver(person).makeReference(person))
               .map(resolvers::tokenize)
               .collect(Collectors.toList());

         Collection<String> targetIds = expandByAuthor(reln.getTargetEntities())
               .map(person -> resolvers.getResolver(person).makeReference(person))
               .map(resolvers::tokenize)
               .collect(Collectors.toList());

         // HACK: reverse role of source and target for analysis and display.
         //       We need to rethink the how SDA relationship types are semantically interpreted.
         //       See https://issues.citd.tamu.edu/browse/SDA-39 for more info
         return RepoAdapter.pairRelated(type, relatedIds, targetIds, (from, to) -> RepoAdapter.createEdge(to, from, reln));
      }
      catch (Exception e)
      {
         // skip any problematic relationships
         String pattern = "Skipping conversion of relationship {0}: encountered an error during adaptation";
         logger.log(Level.WARNING, format(pattern, reln.getId()), e);
         return Stream.empty();
      }
   }

   /**
    * Spread a collection of anchors into a collection of authors.
    * @param workEdge
    * @return
    */
   private Stream<BiographicalEntry> expandByAuthor(Collection<Anchor> anchors)
   {
      try
      {
         return anchors.stream()
            .map(Anchor::getTarget)
            .map(this::resolveWork)
            .flatMap(this::getAuthors);
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, format("unable to extract authors from anchors"), e);
         return Stream.empty();
      }
   }

   /**
    * Looks up the work referenced by the given {@link EntryId}.
    * @param entryId
    * @return
    */
   private BibliographicEntry resolveWork(EntryId entryId)
   {
      try
      {
         return (BibliographicEntry)resolvers.getReference(entryId).getEntry(null);
      }
      catch (ClassCastException e)
      {
         logger.log(Level.WARNING, format("Unable to resolve work from entry id {0} for type {1}", entryId.getId(), entryId.getType()), e);
         return null;
      }
   }

   /**
    * Looks up authors for a given work
    * @param work
    * @return
    */
   private Stream<BiographicalEntry> getAuthors(BibliographicEntry work)
   {
      if (work == null)
      {
         return Stream.empty();
      }

      try
      {
         AuthorList authors = work.getAuthors();

         return StreamSupport.stream(authors.spliterator(), false)
               .map(ref -> {
                  String authorId = ref.getId();

                  return peopleRepo.getOptionally(authorId).orElseGet(() -> {
                     logger.warning(() -> format("work {0} references a non-existent author {1}", work.getId(), authorId));
                     return null;
                  });
               })
               .filter(Objects::nonNull);
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, format("unable to get authors from work {0}", work.getId()), e);
         return Stream.empty();
      }
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

      return buckets.values()
            .stream()
            .filter(bucket -> !bucket.isEmpty())
            .map(this::mergeEqualEdges)
            .collect(Collectors.toList());
   }

   /**
    * Merges a collection of (presumably equivalent) edges into a single edge containing
    * relationshipIds and multiplicity metadata fields
    * @param bucket
    * @return
    */
   private GraphDTO.Edge mergeEqualEdges(Collection<Edge> bucket)
   {
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
   }
}
