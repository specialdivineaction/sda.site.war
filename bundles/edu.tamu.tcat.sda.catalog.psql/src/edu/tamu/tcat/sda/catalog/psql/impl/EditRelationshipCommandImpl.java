package edu.tamu.tcat.sda.catalog.psql.impl;

import java.util.concurrent.Future;
import java.util.function.Function;

import edu.tamu.tcat.sda.catalog.IdFactory;
import edu.tamu.tcat.sda.catalog.relationship.EditRelationshipCommand;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;


public class EditRelationshipCommandImpl implements EditRelationshipCommand
{
   private final RelationshipDV relationship;
   private final IdFactory idFactory;

   private Function<RelationshipDV, Future<String>> commitHook;

   public EditRelationshipCommandImpl(RelationshipDV relationship, IdFactory idFactory)
   {
      this.relationship = relationship;
      this.idFactory = idFactory;
   }

   public void setCommitHook(Function<RelationshipDV, Future<String>> hook)
   {
      commitHook = hook;
   }

}
