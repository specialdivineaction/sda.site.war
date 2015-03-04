package edu.tamu.tcat.sda.catalog.psql.test.restapi;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.services.util.ServiceHelper;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.psql.internal.Activator;
import edu.tamu.tcat.sda.catalog.psql.test.HTTPClient.RestHTTPClient;
import edu.tamu.tcat.sda.catalog.psql.test.PsqlTasks.CleanIdTableDBTask;
import edu.tamu.tcat.sda.catalog.psql.test.PsqlTasks.CleanRelationshipsDBTask;
import edu.tamu.tcat.sda.catalog.psql.test.data.Relationships;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;
import edu.tamu.tcat.trc.entries.reln.rest.v1.model.RelationshipId;

public class TestRelationshipRESTApi
{
   private static URI uri;
   private static JacksonJsonMapper mapper = new JacksonJsonMapper();

   @BeforeClass
   public static void initHTTPConnection() throws Exception
   {
      mapper.activate();
      uri = URI.create("http://localhost:9999/catalog/services/relationships");

      CleanRelationshipsDBTask relnTask = new CleanRelationshipsDBTask();
      CleanIdTableDBTask idTableTask = new CleanIdTableDBTask();
      try (ServiceHelper helper = new ServiceHelper(Activator.getDefault().getContext()))
      {
         SqlExecutor executor = helper.waitForService(SqlExecutor.class, 10000);
         Future<Void> relnFuture = executor.submit(relnTask);
         relnFuture.get();
         Future<Void> idFuture = executor.submit(idTableTask);
         idFuture.get();
      }

   }

   @AfterClass
   public static void tearDown() throws Exception
   {
   }

   @Test
   public void testCreateRelationship()
   {
      RestHTTPClient client = new RestHTTPClient();

      Relationships reln = new Relationships();
      RelationshipDV createdReln = null;
      RelationshipDV relnDV = reln.createRelationshipDV();
      RelationshipId relnId = null;

      try
      {
         client.createPost(uri);
         HttpResponse createResponse = client.postResponse(new StringEntity(mapper.asString(relnDV)));
         try(InputStream is = createResponse.getEntity().getContent())
         {
            relnId = mapper.parse(is, RelationshipId.class);
            client.closeClient();
         }
         catch(IOException ioe)
         {
            fail("IOException Occured cause:" + ioe.getMessage());
         }

         URI getUri = uri.resolve("relationships/" + relnId.id);
         client.createGet(getUri);
         HttpResponse getResponse = client.getResponse();
         try(InputStream is = getResponse.getEntity().getContent())
         {
            createdReln = mapper.parse(is, RelationshipDV.class);
            Assert.assertNotEquals(relnDV.id, createdReln.id);
            Assert.assertEquals(relnDV.typeId, createdReln.typeId);
            Assert.assertEquals(relnDV.description, createdReln.description);
            Assert.assertEquals(relnDV.descriptionMimeType, createdReln.descriptionMimeType);
         }
      }
      catch (Exception e)
      {
            fail("Exception Occured cause:" + e);
      }
   }

   @Test
   public void testUpdateRelationship()
   {
      Relationships reln = new Relationships();
      RelationshipId relnId = null;
      RelationshipDV updateReln = null;
      RelationshipDV originalReln = reln.createRelationshipDV();
      RelationshipDV createdReln = reln.createRelationshipDV();

      try
      {
         RestHTTPClient client = new RestHTTPClient();

         // Create Relationship
         client.createPost(uri);
         HttpResponse createResponse = client.postResponse(new StringEntity(mapper.asString(originalReln)));
         try(InputStream is = createResponse.getEntity().getContent())
         {
            relnId = mapper.parse(is, RelationshipId.class);
            client.closeClient();
         }
         catch(IOException ioe)
         {
            fail("IOException Occured cause:" + ioe.getMessage());
         }

         // Update Created Relationship
         createdReln.id = relnId.id;
         createdReln.typeId = "uk.ac.ox.bodleian.sda.relationships.provoked";

         URI putUri = uri.resolve("relationships/" + createdReln.id);
         client.createPut(putUri);
         HttpResponse putResponse = client.putResponse(new StringEntity(mapper.asString(createdReln)));
         int statusCode = putResponse.getStatusLine().getStatusCode();
         if (statusCode < 200 && statusCode >= 300)
            fail("Response code:" + statusCode);
         client.closeClient();

         // Get the Updated Relationship and compare
         client.createGet(putUri);
         HttpResponse getResponse = client.getResponse();
         try(InputStream is = getResponse.getEntity().getContent())
         {
            updateReln = mapper.parse(is, RelationshipDV.class);
            Assert.assertNotEquals(originalReln.id, updateReln.id);
            Assert.assertNotEquals(originalReln.typeId, updateReln.typeId);
            Assert.assertEquals(originalReln.description, updateReln.description);
            Assert.assertEquals(originalReln.descriptionMimeType, updateReln.descriptionMimeType);
         }

      }
      catch (Exception e)
      {
            fail("Exception Occured cause:" + e);
      }
   }

   @Test
   public void testDeleteRelationship()
   {
      Relationships reln = new Relationships();
      RelationshipId relnId = null;
      RelationshipDV originalReln = reln.createRelationshipDV();

      try
      {
         RestHTTPClient client = new RestHTTPClient();

         // Create Relationship
         client.createPost(uri);
         HttpResponse createResponse = client.postResponse(new StringEntity(mapper.asString(originalReln)));
         try(InputStream is = createResponse.getEntity().getContent())
         {
            relnId = mapper.parse(is, RelationshipId.class);
            client.closeClient();
         }
         catch(IOException ioe)
         {
            fail("IOException Occured cause:" + ioe.getMessage());
         }

         // Deactivate relationship
         URI deleteUri = uri.resolve("relationships/" + relnId.id);
         client.createDelete(deleteUri);
         HttpResponse deleteResponse = client.deleteResponse();
         int statusCode = deleteResponse.getStatusLine().getStatusCode();
         if (statusCode < 200 && statusCode >= 300)
            fail("Response code:" + statusCode);
         client.closeClient();

         AffirmRelationshipsDBTask affirmTask = new AffirmRelationshipsDBTask(relnId.id);
         try (ServiceHelper helper = new ServiceHelper(Activator.getDefault().getContext()))
         {
            SqlExecutor executor = helper.waitForService(SqlExecutor.class, 10000);
            Future<Boolean> relnFuture = executor.submit(affirmTask);

            Assert.assertEquals(false, relnFuture.get());
         }

      }
      catch (Exception e)
      {
            fail("Exception Occured cause:" + e);
      }
   }

   private class AffirmRelationshipsDBTask implements SqlExecutor.ExecutorTask<Boolean>
   {
      final String checkStatus = "SELECT active FROM relationships"
                               + "   WHERE id = ?";
      final String id;

      public AffirmRelationshipsDBTask(String id)
      {
         this.id = id;
      }

      @Override
      public Boolean execute(Connection conn) throws Exception
      {
         try (PreparedStatement ps = conn.prepareStatement(checkStatus))
         {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery())
            {
               if(!rs.next())
                  fail("id was not found in the DB. id:" + id);
               return rs.getBoolean(1);
            }
         }
         catch (SQLException e)
         {
            fail("SQLException thrown:" + e);
         }
         return null;
      }
   }
}
