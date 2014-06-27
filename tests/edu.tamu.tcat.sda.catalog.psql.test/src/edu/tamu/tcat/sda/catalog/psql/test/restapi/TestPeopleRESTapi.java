package edu.tamu.tcat.sda.catalog.psql.test.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.ServiceReference;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.db.psql.PsqlDbExec;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonTypeReference;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.oss.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.oss.osgi.services.util.ServiceHelper;
import edu.tamu.tcat.sda.catalog.people.PeopleRepository;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.catalog.psql.internal.Activator;
import edu.tamu.tcat.sda.catalog.psql.test.data.People;
import edu.tamu.tcat.sda.catalog.rest.ResourceCreationException;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;

public class TestPeopleRESTapi
{
   private static HttpGet  get;
   private static HttpPost post;
   private static HttpDelete delete;
   private static CloseableHttpClient client;
   private static URI uri;
   private static JacksonJsonMapper mapper = new JacksonJsonMapper();
   private ConfigurationProperties properties;

   
   @BeforeClass
   public static void initHTTPConnection()
   {
      mapper.activate();      // might ought to load as OSGi service? 
      uri = URI.create("http://localhost:9999/catalog/services/people");
      client = HttpClientBuilder.create().build();
      
      post = new HttpPost(uri);
      post.setHeader("User-Agent", "Mozilla/5.0");
      post.setHeader("Content-type", "application/json");
      
      get = new HttpGet(uri);
      get.setHeader("User-Agent", "Mozilla/5.0");
      get.setHeader("Content-type", "application/json");
      
      delete = new HttpDelete(uri);
      delete.setHeader("User-Agent", "Mozilla/5.0");
      delete.setHeader("Content-type", "application/json");
   }
   
   @AfterClass
   public static void cleanDB() throws InterruptedException, ExecutionException
   {
      final String cleanDB = "DELETE FROM people";
      DbExecTask<Void> delete = new DbExecTask<Void>()
      {

         @Override
         public Void execute(Connection conn) throws Exception
         {
            try (PreparedStatement ps = conn.prepareStatement(cleanDB))
            {
               ps.executeUpdate();
            } 
            catch (SQLException e)
            {
               throw new SQLException("No records to delete." + e);
            }
            return null;
         }
      };
      
      try (ServiceHelper helper = new ServiceHelper(Activator.getDefault().getContext()))
      {
         DbExecutor executor = helper.waitForService(DbExecutor.class, 10000);
         Future<Void> future = executor.submit(delete);
         
         future.get();
      }
   }

   @Test
   public void testPost() throws Exception
   {

      People person = new People();
      PersonDV buildPerson = person.buildPerson();
      String json = mapper.asString(buildPerson);

      post.setEntity(new StringEntity(json));

      HttpResponse response = client.execute(post);
            
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode >=200 && statusCode < 300)
         Assert.assertTrue("Successfull", (statusCode >=200 && statusCode < 300));
      else if (statusCode >= 300 && statusCode < 400)
         Assert.fail("Redirection: " + statusCode);
      else if (statusCode >= 400 && statusCode < 500)
         Assert.fail("Client Error: " + statusCode);
      else
         Assert.fail("Server Error: " + statusCode);
      
      InputStream content = response.getEntity().getContent();
//      PersonDV person = mapper.fromJSON(content, type)

   }

   @Test
   public void testGetIterable()
   {
      try
      {
         CloseableHttpResponse response = client.execute(get);
         InputStream content = response.getEntity().getContent();
         StatusLine statusLine = response.getStatusLine();
         
         if (statusLine.getStatusCode() < 300)
         {
            try
            {
               List<PersonDV> hfdv = mapper.fromJSON(content, new JsonTypeReference<List<PersonDV>>(){});
               content.close();
            }
            catch (JsonException e)
            {
               e.printStackTrace();
            }
         }
         else
         {
            System.out.println(statusLine.getStatusCode());
         }
      }
      catch (ClientProtocolException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   @Test
   public void testGetPerson()
   {
      try
      {
         URI personUri = uri.resolve("people/1");
         get.setURI(personUri);
         CloseableHttpResponse response = client.execute(get);
         InputStream content = response.getEntity().getContent();
         StatusLine statusLine = response.getStatusLine();
         
         if (statusLine.getStatusCode() < 300)
         {
            try
            {
               PersonDV hfdv = mapper.parse(content, PersonDV.class);
               content.close();
            }
            catch (JsonException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
         else
         {
            System.out.println(statusLine.getStatusCode());
         }
      }
      catch (ClientProtocolException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }


   private static final class CreatePersonObserver extends DataUpdateObserverAdapter<Person>
   {
      private final CountDownLatch latch;

      private volatile Person result;
      private volatile ResourceCreationException exception = null;
      
      CreatePersonObserver()
      {
         this.latch = new CountDownLatch(1);
      }

      @Override
      protected void onFinish(Person result)
      {
         this.result = result;
         this.latch.countDown();
      }
   
      @Override
      protected void onError(String message, Exception ex)
      {
         exception = new ResourceCreationException(message, ex);
         latch.countDown();
      }
      
      public Person getResult(long timeout, TimeUnit units) throws InterruptedException, ResourceCreationException
      {
         latch.await(timeout, units);
         
         if (exception != null)
            throw exception;
         
         Objects.requireNonNull(result, "Repository failed to return created person");
         return result;
      }
   }

}