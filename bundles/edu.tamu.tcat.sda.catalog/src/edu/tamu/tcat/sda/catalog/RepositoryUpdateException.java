package edu.tamu.tcat.sda.catalog;

/**
 * Indicates that an attempt to create or modify resources in a repository failed.
 */
public class RepositoryUpdateException extends Exception
{
   public RepositoryUpdateException()
   {
   }

   public RepositoryUpdateException(String message)
   {
      super(message);
   }

   public RepositoryUpdateException(Throwable cause)
   {
      super(cause);
   }

   public RepositoryUpdateException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public RepositoryUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
