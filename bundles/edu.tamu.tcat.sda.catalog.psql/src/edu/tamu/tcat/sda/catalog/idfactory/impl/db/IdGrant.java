package edu.tamu.tcat.sda.catalog.idfactory.impl.db;

/**
 * Represents a range of identifiers that can be returned. An IdGrant is provided
 * by some underlying gatekeeper (such as a database) that ensure that all grants
 * are issued for distinct id ranges. A grant holder can issue identifiers in the
 * supplied range without returning to the gatekeeper to update the persistent
 * mechanism that guards against duplicate id generation.
 */
public class IdGrant
{
   /** The context for this grant. */
   public final String context;

   /** The first ID authorized to be returned. */
   public final long initial;

   /** The last ID authorized to be returned. */
   public final long limit;

   public IdGrant(String context, long initial, long limit)
   {
      this.context = context;
      this.initial = initial;
      this.limit = limit;
   }
}