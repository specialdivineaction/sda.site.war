package edu.tamu.tcat.sda.catalog.rest.graph.pagerank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
   private static final double EPSILON = 1e-15;
   private static final int MAX_ITERATIONS = 10000;

   private final double credibilityLendingWeight;
   private final List<GraphDTO.Node> nodes;
   private final Map<String, Integer> outDegree;
   private final Map<String, Set<String>> incidentList;

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

      // mapping of nodes available by traversing out-edges
      outDegree = graph.edges.stream()
            .collect(Collectors.groupingBy(e -> e.source, Collectors.summingInt(e -> {
               Integer multiplicity = (Integer)e.metadata.getOrDefault("multiplicity", Integer.valueOf(1));
               return multiplicity.intValue();
            })));

      // mapping of nodes available by reverse-traversing in-edges
      incidentList = graph.edges.stream()
            .collect(Collectors.groupingBy(e -> e.target, Collectors.mapping(e -> e.source, Collectors.toSet())));
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

   private Map<String, Double> computePagerank()
   {
      int numNodes = nodes.size();
      Map<String, Double> pagerank = new HashMap<>(numNodes);
      Map<String, Double> prevPagerank = new HashMap<>(numNodes);

      // initialize pagerank values uniformly
      nodes.forEach(node -> prevPagerank.put(node.id, Double.valueOf(1.0d / numNodes)));

      double dampingTerm = (1.0d - credibilityLendingWeight) / numNodes;

      boolean converged = false;
      int numIter = 0;

      while (!converged && numIter < MAX_ITERATIONS)
      {
         converged = true;
         numIter++;

         // compute new pagerank values for each node
         for (GraphDTO.Node node : nodes)
         {
            Set<String> incidentIds = incidentList.getOrDefault(node.id, Collections.emptySet());
            double incidentCredibility = incidentIds.stream()
                  .mapToDouble(id -> {
                     Integer sourceOutDegree = outDegree.get(id);
                     assert sourceOutDegree != null;
                     return prevPagerank.get(id).doubleValue() / sourceOutDegree.doubleValue();
                  })
                  .sum();

            double oldPagerank = prevPagerank.get(node.id).doubleValue();
            double newPagerank = dampingTerm + credibilityLendingWeight * incidentCredibility;

            if (Math.abs(newPagerank - oldPagerank) > EPSILON)
            {
               converged = false;
            }

            pagerank.put(node.id, Double.valueOf(newPagerank));
         }

         // snapshot values of current iteration for computing next iteration
         pagerank.forEach((id, val) -> prevPagerank.put(id, Double.valueOf(val.doubleValue())));
      }

      // normalize result
      double sum = prevPagerank.values().stream().mapToDouble(pr -> pr.doubleValue()).sum();
      prevPagerank.forEach((id, val) -> pagerank.put(id, Double.valueOf(val.doubleValue() / sum)));

      return pagerank;
   }
}
