package edu.tamu.tcat.sda.catalog.rest.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Java model implementation of JSON Graph Format (JGF).
 *
 * @see http://jsongraphformat.info
 */
public class GraphDTO
{
   /**
    * A container model for a single graph.
    */
   public static class SingleGraph
   {
      /**
       * The contained graph. This value is required.
       */
      public Graph graph;

      /**
       * Factory method to create a new single graph container of the supplied graph.
       * @param graph
       * @return
       */
      public static SingleGraph create(Graph graph)
      {
         SingleGraph dto = new SingleGraph();
         dto.graph = graph;
         return dto;
      }
   }

   /**
    * A container model for a collection of graphs.
    */
   @JsonInclude(Include.NON_NULL)
   public static class MultiGraph
   {
      /**
       * A text label for this collection of graphs.
       */
      public String label;
      /**
       * An optional graph collection classifier.
       */
      public String type;

      /**
       * Additional properties and custom data about this graph collection.
       */
      public Map<String, Object> metadata;

      /**
       * The collection of graphs.
       */
      public List<Graph> graphs;
   }

   @JsonInclude(Include.NON_NULL)
   public static class Graph
   {
      /**
       * A text display label for this graph.
       */
      public String label;

      /**
       * Whether graph edges should be considered to be directed or undirected by default.
       */
      public Boolean directed;

      /**
       * An optional graph type classifier.
       */
      public String type;

      /**
       * Additional properties and custom data about this graph.
       */
      public Map<String, Object> metadata;

      /**
       * A collection of all nodes contained within this graph.
       */
      public List<Node> nodes;

      /**
       * A collection of all edges contained within this graph.
       */
      public List<Edge> edges;
   }

   /**
    * A node or vertex contained within a graph.
    */
   @JsonInclude(Include.NON_NULL)
   public static class Node
   {
      /**
       * An identifier for this node unique within the containing graph.
       * This value is required.
       */
      public String id;

      /**
       * A text display label for this node.
       */
      public String label;

      /**
       * An optional classifier denoting the type of object represented by this node.
       */
      public String type;

      /**
       * Additional properties and custom data about this node.
       */
      public final Map<String, Object> metadata = new HashMap<>();
   }

   /**
    * Connects two nodes in a graph.
    */
   @JsonInclude(Include.NON_NULL)
   public static class Edge
   {
      /**
       * Optional unique identifier for this edge within the containing graph.
       */
      public String id;

      /**
       * The id of this edge's originating node.
       * This value is required.
       */
      public String source;

      /**
       * The id of this edge's terminating node.
       * This value is required.
       */
      public String target;

      /**
       * Describes the relationship between the source and target nodes that this edge represents
       */
      public String relation;

      /**
       * If present, this value overrides the graph's {@code directed} property.
       */
      public Boolean directed;

      /**
       * A text display label for this edge
       */
      public String label;

      /**
       * Additional properties and custom data about this edge.
       */
      public Map<String, Object> metadata;
   }
}
