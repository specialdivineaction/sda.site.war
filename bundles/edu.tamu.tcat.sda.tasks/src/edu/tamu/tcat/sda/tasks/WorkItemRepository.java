package edu.tamu.tcat.sda.tasks;

import java.util.Iterator;
import java.util.Optional;

public interface WorkItemRepository
{

   Iterator<WorkItem> getAllItems();

   Optional<WorkItem> getItem(String id);

   EditWorkItemCommand createItem();

   EditWorkItemCommand editItem(String id);

}
