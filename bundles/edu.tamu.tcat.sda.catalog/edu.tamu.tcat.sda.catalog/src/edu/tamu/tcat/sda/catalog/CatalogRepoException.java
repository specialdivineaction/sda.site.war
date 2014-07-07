package edu.tamu.tcat.sda.catalog;

/**
 * Indicates problems accessing resources within a catalog repository.
 */
public class CatalogRepoException extends Exception
{

   public CatalogRepoException()
   {
   }

   public CatalogRepoException(String message)
   {
      super(message);
   }

   public CatalogRepoException(Throwable cause)
   {
      super(cause);
   }

   public CatalogRepoException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public CatalogRepoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
