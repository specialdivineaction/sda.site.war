package edu.tamu.tcat.trc.entries.bib.copy;

import edu.tamu.tcat.trc.entries.bib.copy.hathitrust.HathiTrustCopyResolver;

public class CopyResolverRegistryImpl implements CopyResolverRegistry
{
   // HACK: this is a throw away impl to get things stitched together.

   @Override
   public <T extends DigitalCopy> T resolve(String identifier, Class<T> copyType) throws ResourceAccessException, UnsupportedCopyTypeException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <T extends DigitalCopy> CopyResolverStrategy<T> getResolver(Class<T> resolverType)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public CopyResolverStrategy<? extends DigitalCopy> getResolver(String identifier) throws UnsupportedCopyTypeException
   {
      HathiTrustCopyResolver htCopyResolve = new HathiTrustCopyResolver();
      if (htCopyResolve.canResolve(identifier))
         return htCopyResolve;

      throw new UnsupportedCopyTypeException("Cannot resolve identifier [" + identifier + "]");
   }

   @Override
   public CopyResolverStrategy<? extends DigitalCopy> getProvider(String id)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
