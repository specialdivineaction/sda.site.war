package edu.tamu.tcat.sda.catalog.rest.graph.pagerank;

public interface PageRank
{
   /**
    * Sets the target metadata field to use if something besides "pagerank" is desired
    *
    * @param field
    */
   void setTargetField(String field);

   /**
    * Sets the pagerank metadata field on each node in the provided graph
    */
   void execute();
}
