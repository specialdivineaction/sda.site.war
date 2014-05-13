package edu.tamu.tcat.oss.db;

import java.util.concurrent.Future;

/**
 * Runs {@link DbExecTask}s
 */
public interface DbExecutor
{
   /**
    * Schedules a task for execution and returns a {@link Future} that the client can use to access the result of this
    * task.
    * 
    * @param task The task to run.
    * @return A {@link Future}
    */
   public <X> Future<X> submit(DbExecTask<X> task);
}
