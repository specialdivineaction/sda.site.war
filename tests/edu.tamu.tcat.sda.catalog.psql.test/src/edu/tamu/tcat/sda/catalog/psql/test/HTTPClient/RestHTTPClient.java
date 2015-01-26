package edu.tamu.tcat.sda.catalog.psql.test.HTTPClient;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class RestHTTPClient
{
   private static CloseableHttpClient client;
   private static HttpGet  get;
   private static HttpPost post;
   private static HttpPut put;
   private static HttpDelete delete;


   public RestHTTPClient()
   {
   }

   public void createGet(URI getUri)
   {
      client = HttpClientBuilder.create().build();
      get = new HttpGet(getUri);
      get.setHeader("User-Agent", "Mozilla/5.0");
      get.setHeader("Content-type", "application/json");
   }

   public void createPost(URI postUri)
   {
      client = HttpClientBuilder.create().build();
      post = new HttpPost(postUri);
      post.setHeader("User-Agent", "Mozilla/5.0");
      post.setHeader("Content-type", "application/json");
   }

   public void createPut(URI putUri)
   {
      client = HttpClientBuilder.create().build();
      put = new HttpPut(putUri);
      put.setHeader("User-Agent", "Mozilla/5.0");
      put.setHeader("Content-type", "application/json");
   }

   public void createDelete(URI putUri)
   {
      client = HttpClientBuilder.create().build();
      delete = new HttpDelete(putUri);
      delete.setHeader("User-Agent", "Mozilla/5.0");
      delete.setHeader("Content-type", "application/json");
   }

   public HttpResponse getResponse() throws ClientProtocolException, IOException
   {
      return client.execute(get);
   }

   public HttpResponse postResponse(StringEntity entity) throws ClientProtocolException, IOException
   {
      post.setEntity(entity);
      return client.execute(post);
   }

   public HttpResponse putResponse(StringEntity entity) throws ClientProtocolException, IOException
   {
      put.setEntity(entity);
      return client.execute(put);
   }

   public HttpResponse deleteResponse() throws ClientProtocolException, IOException
   {
      return client.execute(delete);
   }

   public void closeClient() throws IOException
   {
      client.close();
   }
}
