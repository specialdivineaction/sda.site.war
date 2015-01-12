package edu.tamu.tcat.sda.catalog.psql.idfactory.impl;

import java.util.concurrent.ConcurrentHashMap;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.catalog.IdFactory;

public class DbBackedObfuscatingIdFactory implements IdFactory
{

   private SqlExecutor exec;
   private final ConcurrentHashMap<String, IdGenerator> generators = new ConcurrentHashMap<>();

   public DbBackedObfuscatingIdFactory()
   {
      // TODO Auto-generated constructor stub
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void activate()
   {

   }

   @Override
   public String getNextId(String context)
   {
      if (!generators.contains(context))
      {
         generators.putIfAbsent(context, new IdGenerator(context));
      }

      long id = generators.get(context).next();
      return obfuscate(id);
   }

   private String obfuscate(long id)
   {
      // TODO actually perform obfuscation
      return Long.toString(id);
   }

   private final class GrantProvider
   {
      private final String context;
      private final int grantSize;

      public GrantProvider(String ctx, int size)
      {
         context = ctx;
         this.grantSize = size;
      }

      public IdGrant requestIdGrant() // throws IdGrantCreationException
      {
         try
         {
            GetIdGrantTask task = new GetIdGrantTask(context, grantSize);   // HACK: magic number
            IdGrant idGrant = exec.submit(task).get();
            return idGrant;
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Failed to generate id grant for context [" + context + "]", e);
         }
      }
   }

   private class IdGenerator
   {
      private final GrantProvider grantProvider;
      private IdGrant grant;

      public long nextId = 0;

      public IdGenerator(String context)
      {
         grantProvider = new GrantProvider(context, 20);
      }

      public synchronized long next()
      {
         if (grant == null || this.nextId > grant.limit)
            renewGrant();

         return this.nextId++;
      }

      private void renewGrant()
      {
         grant = grantProvider.requestIdGrant();
         this.nextId = grant.initial;
      }
   }
}
