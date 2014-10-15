package edu.tamu.tcat.sda.catalog.psql;

import java.util.concurrent.atomic.AtomicInteger;

public class IdProvider
{
   private final AtomicInteger counter;

   public IdProvider()
   {
      this(1);
   }

   public IdProvider(int initialValue)
   {
      counter = new AtomicInteger(initialValue);
   }

   public String nextId()
   {
      int id = counter.getAndIncrement();
      return String.valueOf(id);
   }
}
