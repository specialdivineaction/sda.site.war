package edu.tamu.tcat.sda.catalog.psql.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.oss.json.JsonTypeReference;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameRefDV;

public class TestPeopleRESTapi
{
   private static HttpPost post;
   private static HttpGet  get;
   private static CloseableHttpClient client;
   
   @BeforeClass
   public static void initHTTPConnection()
   {
      
      client = HttpClientBuilder.create().build();
      
      post = new HttpPost("http://localhost:9999/catalog/services/people");
      post.setHeader("User-Agent", "Mozilla/5.0");
      post.setHeader("Content-type", "application/json");
      
      get = new HttpGet("http://localhost:9999/catalog/services/people");
      get.setHeader("User-Agent", "Mozilla/5.0");
      get.setHeader("Content-type", "application/json");
   }

   @Test
   public void testPost()
   {
      HistoricalFigureDV histFig = new HistoricalFigureDV();
      
      PersonNameRefDV author = new PersonNameRefDV();
      author.displayName = "George Albert Smith";
      author.familyName = "Smith";
      author.givenName = "George";
      author.middleName = "Albert";
      author.name = "";
      author.suffix = "Sir";
      author.title = "Author";
      
      Set<PersonNameRefDV> authNames = new HashSet<PersonNameRefDV>();
      
      authNames.add(author);
      
      histFig.id = "1234abcd";
      histFig.birth = new Date();
      histFig.death = new Date();
      histFig.people = authNames;
      
      SimpleJacksonMapper map = new SimpleJacksonMapper();
      
      try
      {
         String json = map.asString(histFig);
   
         post.setEntity(new StringEntity(json));
         
         HttpResponse response = client.execute(post);
         int statusCode = response.getStatusLine().getStatusCode();
         if (statusCode > 299)
         {
            System.out.println("Error");
         }
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   @Test
   public void testGet()
   {
      try
      {
         CloseableHttpResponse response = client.execute(get);
         InputStream content = response.getEntity().getContent();
         StatusLine statusLine = response.getStatusLine();

         BufferedReader reader = new BufferedReader(new InputStreamReader(content));
         StringBuilder out = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) {
             out.append(line);
         }
         System.out.println(out.toString());   //Prints the string content read from input stream
         reader.close();
         
         if (statusLine.getStatusCode() < 300)
         {
            SimpleJacksonMapper mapper = new SimpleJacksonMapper();
            try
            {
               List<HistoricalFigureDV> hfdv = mapper.fromJSON(new JsonTypeReference<List<HistoricalFigureDV>>() {}, out.toString()) ;
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
   
   
   private static class SimpleJacksonMapper implements JsonMapper
   {
      ObjectMapper mapper = new ObjectMapper();
      
      @Override
      public String asString(Object o) throws JsonException
      {
         try
         {
            return mapper.writeValueAsString(o);
         }
         catch (JsonProcessingException e)
         {
            throw new JsonException(e);
         }
      }
      
      @Override
      public <T> T parse(String json, Class<T> type) throws JsonException
      {
         try
         {
            return mapper.readValue(json, type);
         }
         catch (IOException e)
         {
            throw new JsonException(e);
         }
      }

      @Override
      public <T> T parse(InputStream is, Class<T> type) throws JsonException
      {
         try
         {
            return mapper.readValue(is, type);
         }
         catch (IOException e)
         {
            throw new JsonException(e);
         }
      }

      @Override
      public <T> T fromJSON(JsonTypeReference<T> type, String jsonPacket) throws JsonException
      {
         T data = null;

         try {
            data = mapper.readValue(jsonPacket, type);
         } catch (Exception e) {
            // Handle the problem
         }
         return data;
      }
      
   }
}
