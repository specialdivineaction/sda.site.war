package edu.tamu.tcat.sda.catalog.rest.graph.pagerank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.tamu.tcat.sda.catalog.rest.graph.GraphDTO;

/**
 * Computes PageRank graph metric for each node in the provided graph and decorates them with a
 * "pagerank" metadata field.
 *
 * This implementation uses an iterative method to arrive at an approximate PageRank value.
 */
public class PageRankIterative implements PageRank
{
   private static final Logger logger = Logger.getLogger(PageRankIterative.class.getName());
   private static final double EPSILON = 1e-15;
   private static final int MAX_ITERATIONS = 10000;

   private final double credibilityLendingWeight;
   private final List<GraphDTO.Node> nodes;
   private final ConcurrentMap<String, Integer> outDegree;
   private final ConcurrentMap<String, Set<String>> incidentList;

   // precomputed values to improve performance
   private final int numNodes;
   private final double dampingTerm;

   private String targetMetadataField = "pagerank";

   /**
    * Creates a new PageRank computation instance over the given graph using a sensible default
    * credibility lending weight of 0.85.
    *
    * When executed, the nodes of the graph will be decorated with a "pagerank" metadata field
    * corresponding to the normalized pagerank value.
    *
    * @param graph
    */
   public PageRankIterative(GraphDTO.Graph graph)
   {
      this(graph, 0.85);
   }

   /**
    * Creates a new PageRank computation instance over the given graph that, when executed,
    * decorates the nodes of the graph with a "pagerank" metadata field corresponding to the
    * normalized PageRank value.
    *
    * @param graph
    * @param credibilityLendingWeight
    */
   public PageRankIterative(GraphDTO.Graph graph, double credibilityLendingWeight)
   {
      if (credibilityLendingWeight <= 0 || credibilityLendingWeight >= 1)
      {
         throw new IllegalArgumentException("Invalid credibility lending weight {" + credibilityLendingWeight + "}: must be in range (0, 1).");
      }

      this.credibilityLendingWeight = credibilityLendingWeight;

      nodes = new ArrayList<>(graph.nodes);
      numNodes = nodes.size();
      dampingTerm = (1.0d - credibilityLendingWeight) / numNodes;

      // mapping of nodes available by traversing out-edges
      outDegree = graph.edges.parallelStream()
            .collect(Collectors.groupingByConcurrent(e -> e.source, Collectors.summingInt(this::getMultiplicity)));

      // mapping of nodes available by reverse-traversing in-edges
      incidentList = graph.edges.parallelStream()
            .collect(Collectors.groupingByConcurrent(e -> e.target, Collectors.mapping(e -> e.source, Collectors.toSet())));
   }

   @Override
   public void setTargetField(String field)
   {
      Objects.requireNonNull(field);
      targetMetadataField = field;
   }

   @Override
   public void execute()
   {
      Map<String, Double> pagerank = computePagerank();
      nodes.forEach(node -> node.metadata.put(targetMetadataField, pagerank.get(node.id)));
   }

   /**
    * Extracts the "multiplicity" metadata field from the given edge or a default multiplicity of 1.
    * @param edge
    * @return
    */
   private int getMultiplicity(GraphDTO.Edge edge)
   {
      try
      {
         Integer multiplicity = (Integer)edge.metadata.getOrDefault("multiplicity", Integer.valueOf(1));
         return multiplicity.intValue();
      }
      catch (ClassCastException e)
      {
         logger.log(Level.WARNING, "Edge contains non-integer 'multiplicity' field.", e);
         return 1;
      }
   }

   /**
    * Computes PageRank values for all nodes.
    * @return A map from each node's id to its PageRank
    */
   private Map<String, Double> computePagerank()
   {
      ConcurrentMap<String, Double> next = new ConcurrentHashMap<>(numNodes);

      // initialize pagerank values uniformly
      ConcurrentMap<String, Double> prev = nodes.parallelStream()
            .collect(Collectors.toConcurrentMap(n -> n.id, n -> Double.valueOf(1.0d / numNodes)));

      boolean converged = false;
      for (int i = 0; !converged && i < MAX_ITERATIONS; i++)
      {
         // compute new pagerank values for each node
         converged = nodes.parallelStream().allMatch(node -> computeNodePagerank(node, prev, next));

         // snapshot values of current iteration for computing next iteration
         prev.putAll(next);
      }

      // normalize result
      double sum = prev.values().parallelStream().mapToDouble(Double::doubleValue).sum();
      prev.replaceAll((id, val) -> Double.valueOf(val.doubleValue() / sum));

      return prev;
   }

   /**
    * Computes a new PageRank value for the given node, updating that node's entry in the given {@code nextPagerank} map
    * @param node
    * @param prev PageRank values of the previous time index
    * @param next Receives the calculated PageRank value
    * @return {@code true} if this node's PageRank has converged
    */
   private boolean computeNodePagerank(GraphDTO.Node node, ConcurrentMap<String, Double> prev, ConcurrentMap<String, Double> next)
   {
      Set<String> incidentIds = incidentList.getOrDefault(node.id, Collections.emptySet());
      double incidentCredibility = incidentIds.parallelStream()
            .mapToDouble(id -> {
               Integer sourceOutDegree = outDegree.get(id);
               assert sourceOutDegree != null;

               Double sourcePagerank = prev.get(id);
               assert sourcePagerank != null;

               return sourcePagerank.doubleValue() / sourceOutDegree.doubleValue();
            })
            .sum();

      Double oldPagerank = prev.get(node.id);
      assert oldPagerank != null;

      double newPagerank = dampingTerm + credibilityLendingWeight * incidentCredibility;

      next.put(node.id, Double.valueOf(newPagerank));

      return Math.abs(newPagerank - oldPagerank.doubleValue()) < EPSILON;
   }
}
