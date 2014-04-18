package edu.tamu.tcat.sda.catalog.psql;

import java.nio.channels.IllegalSelectorException;
import java.sql.Connection;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.sda.ds.DataUpdateObserver;

public class ObservableTaskWrapper<R> implements DbExecTask<R> 
{
   private final DataUpdateObserver<R> observer;
   private final DbExecTask<R> task;
   
   public ObservableTaskWrapper(DbExecTask<R> task, DataUpdateObserver<R> observer)
   {
      this.task = task;
      this.observer = observer;
   }

   @Override
   public R execute(Connection conn)
   {
      if (conn == null)
      {
         String msg = "Cannot execute database task. No connection supplied.";
         observer.error(msg, new IllegalSelectorException());
      }
      
      if (observer.isCanceled())
      {
         observer.aborted();
         return null;
      }

      observer.start(); // notify observer that we are about to start
      try 
      {
         R result = task.execute(conn);
         observer.finish(result);
         return result;
      }
      catch (Exception e)
      {
         observer.error(e.getMessage(), e);
      }
      
      return null;
   }
}
