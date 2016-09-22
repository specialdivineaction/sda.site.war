package edu.tamu.tcat.sda.catalog.rest.graph.pagerank;

import edu.tamu.tcat.sda.catalog.rest.graph.GraphDTO;

/**
 * Computes PageRank graph metric for each node in the provided graph and decorates them with a
 * "pagerank" metadata field.
 *
 * This implementation uses an algebraic method to arrive at the exact PageRank value in a single
 * step. However, this requires finding the principal eigenvector of a square matrix of size equal
 * to the number of nodes in the graph.
 *
 * @deprecated the common math library fails to converge; use iterative approach instead
 */
@Deprecated
public class PageRankEigenvector implements PageRank
{
//   private static final double epsilon = 1e-15;
//
//   private final double credibilityLendingWeight;
//   private final List<GraphDTO.Node> nodes;
//   private final Map<String, Set<String>> adjacencyList;
//
//   private String targetMetadataField = "pagerank";

   /**
    * Creates a new PageRank computation instance over the given graph using a sensible default
    * credibility lending weight of 0.85.
    *
    * When executed, the nodes of the graph will be decorated with a "pagerank" metadata field
    * corresponding to the normalized pagerank value.
    *
    * @param graph
    */
   public PageRankEigenvector(GraphDTO.Graph graph)
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
   public PageRankEigenvector(GraphDTO.Graph graph, double credibilityLendingWeight)
   {
//      if (credibilityLendingWeight <= 0 || credibilityLendingWeight >= 1)
//      {
//         throw new IllegalArgumentException("Invalid credibility lending weight {" + credibilityLendingWeight + "}: must be in range (0, 1).");
//      }
//
//      this.credibilityLendingWeight = credibilityLendingWeight;
//
//      nodes = new ArrayList<>(graph.nodes);
//
//      // mapping of nodes available by traversing out-edges
//      adjacencyList = graph.edges.stream()
//            .collect(Collectors.groupingBy(e -> e.source, Collectors.mapping(e -> e.target, Collectors.toSet())));
   }

   @Override
   public void setTargetField(String field)
   {
//      Objects.requireNonNull(field);
//      targetMetadataField = field;
   }

   @Override
   public void execute()
   {
      throw new UnsupportedOperationException("not implemented");
//      RealMatrix transitionMatrix = computeTransitionMatrix();
//      EigenDecomposition decomposition = new EigenDecomposition(transitionMatrix);
//
//      RealVector pageranks = null;
//
//      double[] eigenvalues = decomposition.getRealEigenvalues();
//      for (int i = 0; i < eigenvalues.length; i++) {
//         if (Math.abs(eigenvalues[i] - 1.0) < epsilon)
//         {
//            pageranks = decomposition.getEigenvector(i);
//         }
//      }
//
//      if (pageranks == null)
//      {
//         throw new IllegalStateException("unable to find principal eigenvector solution for transition matrix");
//      }
//
//      for (int i = 0; i < nodes.size(); i++)
//      {
//         GraphDTO.Node node = nodes.get(i);
//         double pagerank = pageranks.getEntry(i);
//         node.metadata.put(targetMetadataField, Double.valueOf(pagerank));
//      }
   }

//   private RealMatrix computeTransitionMatrix()
//   {
//      int numNodes = nodes.size();
//      double teleportProbability = 1.0 / numNodes;
//
//      // weighted teleporting probability added to all entries
//      double adjustedTeleportProbability = teleportProbability * (1 - credibilityLendingWeight);
//
//      RealMatrix transitionMatrix = new Array2DRowRealMatrix(numNodes, numNodes);
//
//      // each column node index j of the matrix represents the probability of landing on row node index i after one traversal step
//      // hence the sum of the entries in each and every column is 1
//      for (int j = 0; j < numNodes; j++)
//      {
//         GraphDTO.Node source = nodes.get(j);
//         Set<String> adjacentIds = adjacencyList.getOrDefault(source.id, Collections.emptySet());
//
//         if (adjacentIds.isEmpty())
//         {
//            // all entries in column get uniform teleport probability since the only way to reach other nodes is to teleport to them
//            for (int i = 0; i < numNodes; i++)
//            {
//               transitionMatrix.setEntry(i, j, teleportProbability);
//            }
//         }
//         else
//         {
//            double traversalProbability = credibilityLendingWeight / adjacentIds.size();
//
//            for (int i = 0; i < numNodes; i++)
//            {
//               GraphDTO.Node target = nodes.get(i);
//
//               double entryProbability = adjacentIds.contains(target.id) ? traversalProbability : 0;
//               transitionMatrix.setEntry(i, j, entryProbability + adjustedTeleportProbability);
//            }
//         }
//      }
//
//      return transitionMatrix;
//   }
}
