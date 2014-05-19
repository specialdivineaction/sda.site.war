package edu.tamu.tcat.sda.catalog.psql.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
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
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalEventDV;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameRefDV;

public class TestPeopleRESTapi
{
   private static HttpPost post;
   private static CloseableHttpClient client;
   
   @BeforeClass
   public static void initHTTPConnection()
   {
      
      client = HttpClientBuilder.create().build();
      post = new HttpPost("http://localhost:9999/catalog/services/people");
      post.setHeader("User-Agent", "Mozilla/5.0");
      post.setHeader("Content-type", "application/json");
   }

   @Test
   public void test()
   {
      HistoricalFigureDV histFig = new HistoricalFigureDV();
      
      HistoricalEventDV eventBirth = new HistoricalEventDV();
      eventBirth.title = "Date of Birth";
      eventBirth.location = "Boise, Idaho";
      eventBirth.eventDate = new Date();
      eventBirth.description = "This is the birth date of this person, as an example.";
      
      HistoricalEventDV eventDeath = new HistoricalEventDV();
      eventBirth.title = "Date of Death";
      eventBirth.location = "Dallas, Texas";
      eventBirth.eventDate = new Date();
      eventBirth.description = "";
      
      PersonNameRefDV author = new PersonNameRefDV();
      author.displayName = "George Albert Smith";
      author.familyName = "Smith";
      author.givenName = "George";
      author.middleName = "Albert";
      author.name = "";
      author.suffix = "Sir";
      author.title = "Author";
      
      List<HistoricalEventDV> events = new ArrayList<HistoricalEventDV>();
      List<PersonNameRefDV> authNames = new ArrayList<PersonNameRefDV>();
      
      events.add(eventBirth);
      events.add(eventDeath);
      
      authNames.add(author);
      
      histFig.id = "1234abcd";
      histFig.events = events;
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
      
   }
}
