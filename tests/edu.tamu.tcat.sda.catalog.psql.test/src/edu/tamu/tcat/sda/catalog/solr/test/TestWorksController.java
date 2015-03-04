package edu.tamu.tcat.sda.catalog.solr.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonTypeReference;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.solr.WorksController;
import edu.tamu.tcat.trc.entries.bib.dv.WorkDV;

public class TestWorksController
{
   private static HttpGet  get;
   private static CloseableHttpClient client;
   private static URI uri;
   private static JacksonJsonMapper mapper = new JacksonJsonMapper();

   @BeforeClass
   public static void initHTTPConnection()
   {
      mapper.activate();      // might ought to load as OSGi service?
      uri = URI.create("http://localhost:9999/catalog/services/works");
      client = HttpClientBuilder.create().build();

      get = new HttpGet(uri);
      get.setHeader("User-Agent", "Mozilla/5.0");
      get.setHeader("Content-type", "application/json");

   }

   @Test
   public void testAdd() throws Exception
   {
      List<WorkDV> works = getWorks();
      WorksController docs = new WorksController();
      for(WorkDV work : works)
      {
         docs.addDocument(work);
      }
   }

// @Test
// public void cleanAuthors()
// {
//    WorksController docs = new WorksController();
//    docs.cleanAuthors();
// }

   public List<WorkDV> getWorks()
   {
      List<WorkDV> works = null;
      try
      {
         CloseableHttpResponse response = client.execute(get);
         InputStream content = response.getEntity().getContent();
         StatusLine statusLine = response.getStatusLine();

         if (statusLine.getStatusCode() < 300)
         {
            try
            {
               works = mapper.fromJSON(content, new JsonTypeReference<List<WorkDV>>(){});
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
      return works;
   }

}
