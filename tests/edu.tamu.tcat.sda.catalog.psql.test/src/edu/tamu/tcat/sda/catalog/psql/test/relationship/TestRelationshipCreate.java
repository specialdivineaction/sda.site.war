package edu.tamu.tcat.sda.catalog.psql.test.relationship;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.tcat.osgi.services.util.ServiceHelper;
import edu.tamu.tcat.sda.catalog.psql.internal.Activator;
import edu.tamu.tcat.trc.entries.reln.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.reln.RelationshipPersistenceException;
import edu.tamu.tcat.trc.entries.reln.RelationshipRepository;
import edu.tamu.tcat.trc.entries.reln.model.AnchorDV;
import edu.tamu.tcat.trc.entries.reln.model.ProvenanceDV;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;

public class TestRelationshipCreate
{
   private RelationshipRepository repo;
   private RelationshipDV relationship;
   private ProvenanceDV provenance;
   private ServiceHelper helper;

   @Before
   public void setUp() throws Exception
   {
      helper = new ServiceHelper(Activator.getDefault().getContext());
      helper.waitForService(RelationshipRepository.class, 1000);
      relationship = new RelationshipDV();
      relationship.description = "";
      relationship.descriptionMimeType = "";
      relationship.typeId = "";

      provenance = new ProvenanceDV();
      Set<String> uri = new HashSet<>();
      uri.add("related/source/provenance");
      uri.add("related/target/provenance");
      provenance.creatorUris = uri;
      provenance.dateCreated = "String";
      provenance.dateModified = "String";
      relationship.provenance = provenance;

      Set<AnchorDV> related = new HashSet<>();
      related.add(new AnchorDV(uri));
      relationship.relatedEntities = related;

      Set<AnchorDV> target = new HashSet<>();
      target.add(new AnchorDV(uri));
      relationship.targetEntities = target;
   }

   @After
   public void tearDown() throws Exception
   {
      helper.close();
   }

   @Test
   public void testCreate()
   {
      EditRelationshipCommand relationshipCom;
      try
      {
         relationshipCom = repo.create();
         relationshipCom.setAll(relationship);
      }
      catch (RelationshipPersistenceException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      fail("Not yet implemented");
   }

}
