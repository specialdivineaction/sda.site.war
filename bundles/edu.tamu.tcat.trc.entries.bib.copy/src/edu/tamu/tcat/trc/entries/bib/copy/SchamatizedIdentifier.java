package edu.tamu.tcat.trc.entries.bib.copy;

/**
 * Defines a unique identifier that can be used to retrieve a specific digital copy from
 * a {@link CopyResolverStrategy}.
 *
 * @deprecated Currently using String valued identifiers. This seem excessively complex.
 */
@Deprecated
public final class SchamatizedIdentifier
{
   /** An identification scheme. For example, items from HathiTrust will be resolved
    *  using identifiers under the <code>htid</code> scheme. */
   public final String scheme;

   /** The identifier for a digital copy that is unique within the scope of the provided
    *  scheme. */
   public final String uid;

   public SchamatizedIdentifier(String scheme, String uid)
   {
      this.scheme = scheme;
      this.uid = uid;
   }

   private String getKey()
   {
      return scheme + ":" + uid;
   }

   @Override
   public int hashCode()
   {
      return getKey().hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof SchamatizedIdentifier))
            return false;

      SchamatizedIdentifier id = (SchamatizedIdentifier)obj;
      return getKey().equals(id.getKey());
   }
}
