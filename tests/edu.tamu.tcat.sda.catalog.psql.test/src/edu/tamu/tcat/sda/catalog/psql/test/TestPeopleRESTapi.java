package edu.tamu.tcat.sda.catalog.psql.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonTypeReference;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.events.dv.HistoricalEventDV;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameDV;

public class TestPeopleRESTapi
{
   private static HttpPost post;
   private static HttpGet  get;
   private static CloseableHttpClient client;
   private static URI uri;
   private static JacksonJsonMapper mapper = new JacksonJsonMapper();

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
   }

   @Test
   public void testPost() throws JsonException, ClientProtocolException, IOException
   {
      HistoricalFigureDV histFig = new HistoricalFigureDV();

      PersonNameDV author = new PersonNameDV();

      author.displayName = "George Albert Smith";
      author.familyName = "Smith";
      author.givenName = "George";
      author.middleName = "Albert";
      author.name = "";
      author.suffix = "Sir";
      author.title = "Author";

      Set<PersonNameDV> authNames = new HashSet<PersonNameDV>();

      authNames.add(author);

      histFig.id = "1234abcd";
      // TODO create a new DateOfDeath/Birth class
      histFig.birth = new HistoricalEventDV();
      histFig.birth.title = "Date of birth for " + author.displayName;
      histFig.birth.eventDate = new Date();

      histFig.death = new HistoricalEventDV();
      histFig.death.title = "Date of death for " + author.displayName;
      histFig.death.eventDate = new Date();
      histFig.people = authNames;

      String json = mapper.asString(histFig);

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
   }

   @Test
   public void testGetIterable() throws JsonException, ClientProtocolException, IOException
   {
      try (CloseableHttpResponse response = client.execute(get);
           InputStream content = response.getEntity().getContent();
           BufferedReader reader = new BufferedReader(new InputStreamReader(content)))
      {
         StatusLine statusLine = response.getStatusLine();
         if (statusLine.getStatusCode() < 300)
         {
            try
            {
               List<HistoricalFigureDV> hfdv = mapper.fromJSON(content, new JsonTypeReference<List<HistoricalFigureDV>>(){});
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
	         // FIXME sysout call
            System.out.println(statusLine.getStatusCode());
         }
      }
   }

   @Test
   public void testGetPerson()
   {
      try (CloseableHttpResponse response = client.execute(get);
           InputStream content = response.getEntity().getContent())
      {
         URI personUri = uri.resolve("people/16");
         get.setURI(personUri);

         StatusLine statusLine = response.getStatusLine();

         if (statusLine.getStatusCode() < 300)
         {
            try
            {
               @SuppressWarnings("unused")  // test json deserialization
               HistoricalFigureDV hfdv = mapper.parse(content, HistoricalFigureDV.class);
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
}
