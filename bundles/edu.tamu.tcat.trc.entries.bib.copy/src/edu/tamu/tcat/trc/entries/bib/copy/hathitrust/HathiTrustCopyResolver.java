package edu.tamu.tcat.trc.entries.bib.copy.hathitrust;

import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverStrategy;
import edu.tamu.tcat.trc.entries.bib.copy.ResourceAccessException;

public class HathiTrustCopyResolver implements CopyResolverStrategy<HathiTrustCopy>
{

   public HathiTrustCopyResolver()
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public boolean canResolve(String identifier)
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public HathiTrustCopy resolve(String identifier) throws ResourceAccessException, IllegalArgumentException
   {
      // TODO Auto-generated method stub
      return null;
   }

}
