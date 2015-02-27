package edu.tamu.tcat.sda.catalog.copies;


public interface DigitalContentReference
{
   /**
    * @return The service provider associated with a digital copy (HathiTrust, Google, Scanned PDF, etc..)
    */
   DigitalCopyProvider getProvider();

   /**
    * @return The record number created by the service provider.
    */
   String getRecordNumber();

   /**
    * @return The access available for a digital content, defined by the service provider
    */
   String getAccess();

   /**
    * @return The rights to access a digital content, defined by the service provider
    */
   String getRights();

   /**
    * @return The originating location of the digital copy.
    */
   String getSource();

   /**
    * @return The record number of the digital copy from the source location
    */
   String getSourceRecordNumber();

   /**
    * @return The title of the original copy
    */
   String getTitle();
}
