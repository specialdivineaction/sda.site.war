package edu.tamu.tcat.sda.catalog.relationship.model.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.sda.catalog.relationship.Anchor;
import edu.tamu.tcat.sda.catalog.relationship.AnchorSet;

public class BasicAnchorSet implements AnchorSet
{
   private final Set<Anchor> anchors;

   public BasicAnchorSet(Set<Anchor> anchors)
   {
      this.anchors = new HashSet<>(anchors);
   }

   @Override
   public Collection<Anchor> getAnchors()
   {
      return Collections.unmodifiableCollection(anchors);
   }
}
