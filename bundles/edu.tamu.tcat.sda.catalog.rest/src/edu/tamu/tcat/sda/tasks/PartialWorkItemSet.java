package edu.tamu.tcat.sda.tasks;

import java.util.List;

/**
 *  Represents a partial set of of items returned in response to a query.
 *
 */
public interface PartialWorkItemSet
{

   int getTotalMatched();

   int getStart();

   List<WorkItem> getItems();

   PartialWorkItemSet getNext();

}
