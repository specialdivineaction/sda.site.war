package edu.tamu.tcat.sda.catalog;

public class PGObjectNotSupportedException extends Exception
{
   public PGObjectNotSupportedException()
   {
   }

   public PGObjectNotSupportedException(String arg0)
   {
      super(arg0);
   }

   public PGObjectNotSupportedException(Throwable arg0)
   {
      super(arg0);
   }

   public PGObjectNotSupportedException(String arg0, Throwable arg1)
   {
      super(arg0, arg1);
   }

   public PGObjectNotSupportedException(String arg0, Throwable arg1, boolean arg2, boolean arg3)
   {
      super(arg0, arg1, arg2, arg3);
   }
}
