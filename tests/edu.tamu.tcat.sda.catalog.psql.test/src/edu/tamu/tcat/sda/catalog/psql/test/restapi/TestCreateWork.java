package edu.tamu.tcat.sda.catalog.psql.test.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import edu.tamu.tcat.oss.json.JsonTypeReference;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.psql.test.data.Works;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;


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
	public void testCreate() throws JsonException, ClientProtocolException, IOException
	{
	   Works workDb = new Works();
		String json = mapper.asString(workDb.buildWork());
      StringEntity sEntity = new StringEntity(json, ContentType.create("application/json", "UTF-8"));
      post.setEntity(sEntity);

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

		WorkDV createdWork = mapper.parse(response.getEntity().getContent(), WorkDV.class);

		// The created work does not contain an ID until after the creation.
		// TODO: Add additional checking for commetted out Objects
//		Assert.assertEquals(workDb.workDV.authors, createdWork.authors);
//		Assert.assertEquals(workDb.workDV.otherAuthors, createdWork.otherAuthors);
//		Assert.assertEquals(workDb.workDV.titles, createdWork.titles);
//		Assert.assertEquals(workDb.workDV.pubInfo, createdWork.pubInfo);
		Assert.assertEquals(workDb.workDV.series, createdWork.series);
		Assert.assertEquals(workDb.workDV.summary, createdWork.summary);

	}

	@Test
	public void testUpdate() throws JsonException, ClientProtocolException, IOException
	{
      Works workDb = new Works();
      String json = mapper.asString(workDb.buildWork());
      StringEntity sEntity = new StringEntity(json, ContentType.create("application/json", "UTF-8"));
      post.setEntity(sEntity);

      HttpResponse response = client.execute(post);
      InputStream content = response.getEntity().getContent();

      WorkDV createdWork = mapper.parse(content, WorkDV.class);
      createdWork.summary = "Change to a new Summary";

      put.setURI(uri.resolve("works/" + createdWork.id));
      String updatedJson = mapper.asString(createdWork);
      StringEntity updatedEntity = new StringEntity(updatedJson, ContentType.create("application/json", "UTF-8"));
      put.setEntity(updatedEntity);

      HttpResponse updatedResponse = client.execute(put);
      InputStream updatedContent = updatedResponse.getEntity().getContent();
      WorkDV updatedWork = mapper.parse(updatedContent, WorkDV.class);
      Assert.assertEquals(workDb.workDV.series, updatedWork.series);
      Assert.assertNotEquals(workDb.workDV.summary, updatedWork.summary);

	}

	@Test
	public void testSearchTitle() throws JsonException, ClientProtocolException, IOException
	{
      Works workDb = new Works();
      String json = mapper.asString(workDb.buildWork());
      StringEntity sEntity = new StringEntity(json, ContentType.create("application/json", "UTF-8"));
      post.setEntity(sEntity);

      HttpResponse response = client.execute(post);
      try (InputStream content = response.getEntity().getContent())
      {
         WorkDV createdWork = mapper.parse(content, WorkDV.class);
      }
      catch(IOException e)
      {
         throw new IllegalStateException("");
      }
      get.setURI(uri.resolve("works/?title=comp"));
      HttpResponse getResponse = client.execute(get);
      InputStream getContent = getResponse.getEntity().getContent();

      List<WorkDV> searchedWorks = mapper.fromJSON(getContent, new JsonTypeReference<List<WorkDV>>(){});

	}

//	@Test
//	public void testGet() throws ClientProtocolException, IOException
//	{
//	   CloseableHttpResponse response = client.execute(get);
//      InputStream content = response.getEntity().getContent();
//      StatusLine statusLine = response.getStatusLine();
//	}

//   @Test
//   public void testWork() throws ClientProtocolException, IOException
//   {
//      URI personUri = uri.resolve("works/16");
//      get.setURI(personUri);
//      CloseableHttpResponse response = client.execute(get);
//      InputStream content = response.getEntity().getContent();
//      StatusLine statusLine = response.getStatusLine();
//   }
}
