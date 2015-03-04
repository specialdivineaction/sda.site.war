package edu.tamu.tcat.sda.catalog.psql.test.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.psql.test.data.Works;
import edu.tamu.tcat.trc.entries.bib.dv.CustomResultsDV;
import edu.tamu.tcat.trc.entries.bib.dv.EditionDV;
import edu.tamu.tcat.trc.entries.bib.dv.VolumeDV;
import edu.tamu.tcat.trc.entries.bib.dv.WorkDV;


public class TestCreateWork
{
   private static HttpPost post;
   private static HttpPut put;
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

      post = new HttpPost(uri);
      post.setHeader("User-Agent", "Mozilla/5.0");
      post.setHeader("Content-type", "application/json");

      put = new HttpPut(uri);
      put.setHeader("User-Agent", "Mozilla/5.0");
      put.setHeader("Content-type", "application/json");

      get = new HttpGet(uri);
      get.setHeader("User-Agent", "Mozilla/5.0");
      get.setHeader("Content-type", "application/json");
   }

	@Test
	public void testWork() throws JsonException, IOException
	{
	   Works workDb = new Works();
	   WorkDV workOrig = workDb.addWork();
	   String workId = addWork(workOrig);

      try (InputStream getStream = get(URI.create("works/" + workId)))
      {
         WorkDV  workComp = mapper.parse(getStream, WorkDV.class);
         Assert.assertEquals(workOrig.series, workComp.series);
         Assert.assertEquals(workOrig.summary, workComp.summary);
      }
	}

   @Test
   public void testEdition() throws JsonException, IOException
   {
      Works workDb = new Works();

      WorkDV workOrig = workDb.addWork();
      String workId = addWork(workOrig);

      EditionDV edition = workDb.addEdition();
      String editionId = addEdition(edition, workId);

      try (InputStream getStream = get(URI.create("works/" + workId + "/editions/" + editionId)))
      {
         EditionDV createdEdition = mapper.parse(getStream, EditionDV.class);
         Assert.assertEquals(edition.series, createdEdition.series);
         Assert.assertEquals(edition.summary, createdEdition.summary);
      }
   }

   @Test
   public void testVolume() throws JsonException, IOException
   {
      Works workDb = new Works();

      WorkDV workOrig = workDb.addWork();
      String workId = addWork(workOrig);

      EditionDV edition = workDb.addEdition();
      String editionId = addEdition(edition, workId);

      VolumeDV volume = workDb.addVolume();
      String volumeId = addVolume(volume, workId, editionId);

      try (InputStream getStream = get(URI.create("works/" + workId + "/editions/"
                                              + editionId + "/volumes/" + volumeId)))
      {
         VolumeDV createdVolume = mapper.parse(getStream, VolumeDV.class);
         Assert.assertEquals(volume.series, createdVolume.series);
         Assert.assertEquals(volume.summary, createdVolume.summary);
      }
   }

	String addWork(WorkDV original) throws JsonException, IOException
	{
	   String json = mapper.asString(original);
	   StringEntity sEntity = new StringEntity(json, ContentType.create("application/json", "UTF-8"));
	   post.setEntity(sEntity);
	   try (InputStream postStream = post(uri))
	   {
	      CustomResultsDV workResult = mapper.parse(postStream, CustomResultsDV.class);
	      return workResult.id;
	   }
	}

	String addEdition(EditionDV original, String workId) throws JsonException, IOException
	{
	   String json = mapper.asString(original);
	   StringEntity sEntity = new StringEntity(json, ContentType.create("application/json", "UTF-8"));
      post.setEntity(sEntity);
      try (InputStream postStream = post(URI.create("works/" + workId + "/editions")))
      {
         CustomResultsDV workResult = mapper.parse(postStream, CustomResultsDV.class);
         return workResult.id;
      }
	}

	String addVolume(VolumeDV original, String workID, String editionId) throws JsonException, IOException
	{
	   String json = mapper.asString(original);
	   StringEntity sEntity = new StringEntity(json, ContentType.create("application/json", "UTF-8"));
	   post.setEntity(sEntity);
	   try (InputStream postStream = post(URI.create("works/" + workID + "/editions/" + editionId + "/volumes")))
	   {
	      CustomResultsDV workResult = mapper.parse(postStream, CustomResultsDV.class);
	      return workResult.id;
	   }
	}

   InputStream get(URI getUri)
   {
      HttpResponse response;
      if (!getUri.equals(get))
         get.setURI(uri.resolve(getUri));
      try
      {
         response =  client.execute(get);
         if (checkResponse(response.getStatusLine().getStatusCode()))
            return response.getEntity().getContent();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   InputStream post(URI postUri)
   {
      HttpResponse response;
      if (!postUri.equals(uri))
         post.setURI(uri.resolve(postUri));

      try
      {
         response = client.execute(post);
         if (checkResponse(response.getStatusLine().getStatusCode()))
            return response.getEntity().getContent();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   InputStream put(URI putUri)
   {
      if (!putUri.equals(uri))
         put.setURI(uri.resolve(putUri));

      HttpResponse response;
      try
      {
         response = client.execute(put);
         if (checkResponse(response.getStatusLine().getStatusCode()))
            return response.getEntity().getContent();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   Boolean checkResponse(int statusCode)
   {
      if (statusCode >=200 && statusCode < 300)
      {
         Assert.assertTrue("Successfull", (statusCode >=200 && statusCode < 300));
         return true;
      }
      else if (statusCode >= 300 && statusCode < 400)
      {
         Assert.fail("Redirection: " + statusCode);
         return false;
      }
      else if (statusCode >= 400 && statusCode < 500)
      {
         Assert.fail("Client Error: " + statusCode);
         return false;
      }
      else
      {
         Assert.fail("Server Error: " + statusCode);
         return false;
      }
   }


}
