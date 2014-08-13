package edu.tamu.tcat.sda.catalog.psql.test.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.DateDescriptionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;


public class TestCreateWork
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

      uri = URI.create("http://localhost:9999/catalog/services/works");
      client = HttpClientBuilder.create().build();

      post = new HttpPost(uri);
      post.setHeader("User-Agent", "Mozilla/5.0");
      post.setHeader("Content-type", "application/json");

      get = new HttpGet(uri);
      get.setHeader("User-Agent", "Mozilla/5.0");
      get.setHeader("Content-type", "application/json");
   }


	@Test
	public void testCreate() throws JsonException, ClientProtocolException, IOException
	{
	   List<AuthorRefDV> authorList = new ArrayList<>();
	   List<AuthorRefDV> otherAuthorList = new ArrayList<>();

		AuthorRefDV authorRef = new AuthorRefDV();
		authorRef.authorId = "1234";
		authorRef.name = "A.C. Dixon";
		authorRef.role = "Autor";

		authorList.add(authorRef);

		TitleDV canonical = new TitleDV();
		canonical.title = "Canonical Full Title Name";
		canonical.subtitle = "With subtitle";
		canonical.lg = "EN";
		canonical.type = "canonical";

		TitleDV shortTitle = new TitleDV();
		shortTitle.title = "Short Title";
		shortTitle.subtitle = "";
		shortTitle.lg = "EN";
		shortTitle.type = "short";

		TitleDV localeTitle = new TitleDV();
		localeTitle.title = "Locale Nombre completo Titulo";
		localeTitle.subtitle = "";
		localeTitle.lg = "ES";
		localeTitle.type = "locale";

      // Alternative Titles
      TitleDV alt1 = new TitleDV();
      alt1.title = "Alternate Title 1";
      alt1.subtitle = "";
      alt1.lg = "EN";
      alt1.type = "alt";

      TitleDV alt2 = new TitleDV();
      alt2.title = "Alternate Title 2";
      alt2.subtitle = "";
      alt2.lg = "EN";
      alt2.type = "alt";

      Set<TitleDV> titleSet = new HashSet<TitleDV>();
      titleSet.add(canonical);
      titleSet.add(shortTitle);
      titleSet.add(localeTitle);
      titleSet.add(alt1);
      titleSet.add(alt2);


		DateDescriptionDV dateDescript = new DateDescriptionDV();
		dateDescript.display = "";
		dateDescript.value = new Date();

		PublicationInfoDV pubInfo = new PublicationInfoDV();
		pubInfo.date = dateDescript;
		pubInfo.place = "";
		pubInfo.publisher = "";

		WorkDV works = new WorkDV();
		works.authors = authorList;
		works.otherAuthors = otherAuthorList;
		works.titles = titleSet;
		works.pubInfo = pubInfo;
		works.series = "Series 1";
		works.summary = "Summary of the work";

		String json = mapper.asString(works);
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


	}

	@Test
	public void testGet() throws ClientProtocolException, IOException
	{
	   CloseableHttpResponse response = client.execute(get);
      InputStream content = response.getEntity().getContent();
      StatusLine statusLine = response.getStatusLine();
	}

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
