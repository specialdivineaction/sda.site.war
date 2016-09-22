package edu.tamu.tcat.sda.tasks;

import java.util.Iterator;

public interface WorkItemRepository
{

   Iterator<WorkItem> getAllItems();

   WorkItem getItem(String id);

   EditWorkItemCommand createItem();

   EditWorkItemCommand editItem(String id);

}
