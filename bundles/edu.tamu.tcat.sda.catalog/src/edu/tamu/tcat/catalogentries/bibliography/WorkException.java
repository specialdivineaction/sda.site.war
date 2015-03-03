package edu.tamu.tcat.catalogentries.bibliography;

public class WorkException extends Exception
{

   public WorkException()
   {
   }

   public WorkException(String message)
   {
      super(message);
   }

   public WorkException(Throwable cause)
   {
      super(cause);
   }

   public WorkException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public WorkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
