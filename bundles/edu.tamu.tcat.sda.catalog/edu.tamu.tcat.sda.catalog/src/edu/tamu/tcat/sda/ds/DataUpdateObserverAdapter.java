package edu.tamu.tcat.sda.ds;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DataUpdateObserverAdapter<R> implements DataUpdateObserver<R>
{
   private static final Logger logger = Logger.getLogger("org.tamu.tcat.sda.datastore.errors");
   
   private volatile boolean cancelled = false;
   private volatile State state = State.PENDING;
   
   public DataUpdateObserverAdapter()
   {
   }

   @Override
   public final boolean start()
   {
      synchronized (state)
      {
         if (isCompleted())
            throw new IllegalStateException();
         
         try 
         {
            if (this.onStart())
            {
               state = State.STARTED;
               return true;
            }
         }
         catch (Exception e) 
         {
            logger.log(Level.SEVERE, "DataUpdateAdapter error: onStart failed [" + this + "]", e);
         }
         
         return false;
      }
   }

   @Override
   public final void finish(R result)
   {
      try 
      {
         synchronized (state)
         {
            if (isCompleted())
               throw new IllegalStateException();

            this.onFinish(result);
            state = State.COMPLETED;
         }
      }
      catch (Exception e) 
      {
         logger.log(Level.SEVERE, "DataUpdateAdapter error: onFinish failed [" + this + "]", e);
      }
   }

   @Override
   public void aborted()
   {
      try 
      {
         synchronized (state)
         {
            if (isCompleted())
               throw new IllegalStateException();

            this.onAborted();
            state = State.ABORTED;
         }
      }
      catch (Exception e) 
      {
         logger.log(Level.SEVERE, "DataUpdateAdapter error: onAborted failed [" + this + "]", e);
      }
      
   }

   @Override
   public final void error(String message, Exception ex)
   {
      synchronized (state)
      {
         if (isCompleted())
            throw new IllegalStateException();
         
         this.state = State.ERROR;

         try 
         {
            this.onError(message, ex);
         }
         catch (Exception e) 
         {
            e.addSuppressed(ex);
            logger.log(Level.SEVERE, "DataUpdateAdapter error: onError failed. '" + message + "' [" + ex + "]", e);
         }
      }
   }

   @Override
   public final boolean isCanceled()
   {
      return cancelled;
   }
   
   public final void cancel()
   {
      this.cancelled = true;
   }

   @Override
   public final boolean isCompleted()
   {
      return !(state == State.PENDING || state == State.STARTED);
   }

   @Override
   public final State getState()
   {
      return state;
   }

   protected void onAborted()
   {
      // default no-op implementation
      
   }

   /**
    * Allows 
    * @return {@code false} If processing should be aborted at this stage.
    */
   protected boolean onStart() {
      return true;
   }

   protected void onFinish(R result)
   {
      // default no-op implementation
   }

   protected void onError(String message, Exception ex)
   {
      // default no-op implementation
   }

}
