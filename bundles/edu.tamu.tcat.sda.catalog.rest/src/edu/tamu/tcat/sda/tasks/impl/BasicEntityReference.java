package edu.tamu.tcat.sda.tasks.impl;

import edu.tamu.tcat.trc.repo.EntityReference;

class BasicEntityReference implements EntityReference
{
   private final String id;
   private final String type;

   public BasicEntityReference(String id, String type)
   {
      this.id = id;
      this.type = type;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getType()
   {
      return type;
   }
}