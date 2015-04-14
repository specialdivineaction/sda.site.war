package edu.tamu.tcat.trc.entries.bib.copy;

public interface CopyResolverRegistry
{

   /**
    * Instantiates a
    * @param identifier
    * @param copyType
    * @return
    * @throws ResourceAccessException
    * @throws UnsupportedCopyTypeException
    */
   <T extends DigitalCopy> T resolve(String identifier, Class<T> copyType) throws ResourceAccessException, UnsupportedCopyTypeException;

   <T extends DigitalCopy> CopyResolverStrategy<T> getResolver(Class<T> resolverType);

   CopyResolverStrategy<? extends DigitalCopy> getResolver(String identifier) throws UnsupportedCopyTypeException;

   CopyResolverStrategy<? extends DigitalCopy> getProvider(String id);
}
