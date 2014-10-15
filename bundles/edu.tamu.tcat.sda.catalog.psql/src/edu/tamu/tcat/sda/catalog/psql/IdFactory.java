package edu.tamu.tcat.sda.catalog.psql;

import java.util.concurrent.atomic.AtomicInteger;

public class IdFactory
{
   private final AtomicInteger counter;

   public IdFactory()
   {
      this(1);
   }

   public IdFactory(int initialValue)
   {
      counter = new AtomicInteger(initialValue);
   }

   public String nextId()
   {
      int id = counter.getAndIncrement();
      return String.valueOf(id);
   }
}
