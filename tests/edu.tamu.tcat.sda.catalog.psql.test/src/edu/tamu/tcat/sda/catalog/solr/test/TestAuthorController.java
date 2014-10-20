package edu.tamu.tcat.sda.catalog.solr.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonTypeReference;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.events.dv.HistoricalEventDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameDV;
import edu.tamu.tcat.sda.catalog.solr.AuthorController;

public class TestAuthorController
{
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

      get = new HttpGet(uri);
      get.setHeader("User-Agent", "Mozilla/5.0");
      get.setHeader("Content-type", "application/json");
      cleanAuthors();
   }

   @Test
   public void testAdd() throws Exception
   {
      List<PersonDV> authors = getAuthors();
      AuthorController docs = new AuthorController();
      for(PersonDV author : authors)
      {
         docs.addDocument(author);
      }
   }


   private static void cleanAuthors()
   {
      AuthorController docs = new AuthorController();
      docs.clean();
   }

   public List<PersonDV> getAuthors()
   {
      List<PersonDV> authors = null;
      try
      {
         CloseableHttpResponse response = client.execute(get);
         InputStream content = response.getEntity().getContent();
         StatusLine statusLine = response.getStatusLine();

         if (statusLine.getStatusCode() < 300)
         {
            try
            {
               authors = mapper.fromJSON(content, new JsonTypeReference<List<PersonDV>>(){});
               content.close();
            }
            catch (JsonException e)
            {
               e.printStackTrace();
            }
         }
         else
         {
            Assert.fail("Redirection: " + statusLine.getStatusCode());
         }
      }
      catch (ClientProtocolException e)
      {
         Assert.fail("Client Error" + e);
      }
      catch (IOException e)
      {
         Assert.fail("IOException" + e);
      }
      return authors;
   }
}
