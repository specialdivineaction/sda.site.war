package edu.tamu.tcat.sda.tasks.rest.v1;

/**
 *  Implements the REST API for the list of work items associated with a particular
 *  taks. This API is scoped to a particular user account so that the returned items reflect
 *  that account's view of the work to be performed within a task.
 */
public class WorklistResource
{

   public RestApiV1.WorkItem getItem(String id)
   {
      throw new UnsupportedOperationException();
   }

   public RestApiV1.GroupedWorklist listItems(RestApiV1.WorklistQuery query)
   {
      throw new UnsupportedOperationException();
   }

   public void addWorkItem(RestApiV1.WorkItem item)
   {
      throw new UnsupportedOperationException();

   }


}
