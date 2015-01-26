package edu.tamu.tcat.sda.catalog.psql.test.relationship;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.services.util.ServiceHelper;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.psql.internal.Activator;
import edu.tamu.tcat.sda.catalog.psql.test.PsqlTasks.CleanIdTableDBTask;
import edu.tamu.tcat.sda.catalog.psql.test.PsqlTasks.CleanRelationshipsDBTask;
import edu.tamu.tcat.sda.catalog.psql.test.data.Relationships;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

public class TestRelationshipRESTApiCreate
{
   private static HttpGet  get;
   private static HttpPost post;
   private static CloseableHttpClient client;
   private static URI uri;
   private static JacksonJsonMapper mapper = new JacksonJsonMapper();

   @BeforeClass
   public static void initHTTPConnection() throws Exception
   {
      mapper.activate();
      uri = URI.create("http://localhost:9999/catalog/services/relationships");
      client = HttpClientBuilder.create().build();

      post = new HttpPost(uri);
      post.setHeader("User-Agent", "Mozilla/5.0");
      post.setHeader("Content-type", "application/json");

      get = new HttpGet(uri);
      get.setHeader("User-Agent", "Mozilla/5.0");
      get.setHeader("Content-type", "application/json");

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
      Relationships reln = new Relationships();
      RelationshipDV createdReln = null;
      RelationshipDV relnDV = reln.createRelationshipDV();

      try
      {
         post.setEntity(new StringEntity(mapper.asString(relnDV)));

         HttpResponse response = client.execute(post);
         try(InputStream is = response.getEntity().getContent())
         {
            createdReln = mapper.parse(is, RelationshipDV.class);
         }
         catch(IOException ioe)
         {
            fail("IOException Occured cause:" + ioe.getMessage());
         }

         URI relationshipUri = uri.resolve("relationships/" + createdReln.id);
         get.setURI(relationshipUri);

         CloseableHttpResponse clResponse = client.execute(get);
         try(InputStream is = clResponse.getEntity().getContent())
         {
            RelationshipDV compareReln = mapper.parse(is, RelationshipDV.class);

            Assert.assertEquals(relnDV.description, compareReln.description);
         }
      }
      catch (Exception e)
      {
            fail("Exception Occured cause:" + e);
      }
   }
}
