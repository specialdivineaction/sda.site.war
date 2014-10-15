package edu.tamu.tcat.sda.catalog.psql.idfactory.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import edu.tamu.tcat.sda.catalog.psql.idfactory.IdFactory;

public class IdFactoryImpl implements IdFactory
{
   /**
    * Stores a mapping of context to the <em>next</em> ID in the system.
    */
   private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();


   public void activate()
   {
      // TODO: find initial values for counters
      HashMap<String, Long> initialValues = new HashMap<>();

      initialValues.forEach((k,v) -> counters.put(k, new AtomicLong(v.longValue())));
   }

   public void dispose()
   {
      // TODO: save counters or save on-the-fly
   }

   @Override
   public String getNextId(String context)
   {
      counters.putIfAbsent(context, new AtomicLong(1));
      long id = counters.get(context).getAndIncrement();
      return String.valueOf(id);
   }
}
