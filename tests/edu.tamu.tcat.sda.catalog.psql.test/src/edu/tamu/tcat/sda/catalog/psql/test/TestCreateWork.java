package edu.tamu.tcat.sda.catalog.psql.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.oss.db.psql.DataSourceFactory;
import edu.tamu.tcat.oss.db.psql.PsqlDbExec;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.oss.json.JsonTypeReference;
import edu.tamu.tcat.sda.catalog.psql.PsqlWorkRepo;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDv;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.ds.DataUpdateObserverAdapter;


public class TestCreateWork 
{
   private static PsqlDbExec dbExec;

   @BeforeClass
   public static void initDbConnection()
   {
      // TODO make configurable
      // FIXME at the moment, we have no way to clean up after tests!!
      String url = "jdbc:postgresql://localhost:5433/SDA";
      String user = "postgres";
      String pass = "";
      
      
      DataSourceFactory factory = new DataSourceFactory();
      dbExec = new PsqlDbExec(factory.getDataSource(url, user, pass));
   }
   
   @AfterClass
   public static void tearDown() 
   {
      if (dbExec != null)
         dbExec.close();
   }
   
	@Test
	public void testCreate() 
	{
		AuthorRefDv authorRef = new AuthorRefDv();
		authorRef.authorId = "1234";
		authorRef.name = "A.C. Dixon";
		authorRef.role = "";
		
		List<AuthorRefDv> authorList = new ArrayList<>();
		authorList.add(authorRef);
		
		
		WorkDV works = new WorkDV();
		works.id = UUID.randomUUID().toString();
		works.authors = authorList;
		
		// FIXME this is async, meaning test will exit prior to conclusion.
		final CountDownLatch latch = new CountDownLatch(1);
		
		PsqlWorkRepo workRepo = new PsqlWorkRepo(dbExec, new SimpleJacksonMapper());
      workRepo.create(works, new DataUpdateObserverAdapter<Work>()
      {
         @Override
         protected void onFinish(Work result)
         {
            System.out.println("Sucess!");
            latch.countDown();
         }
         
         @Override
         protected void onError(String message, Exception ex)
         {
            assertFalse(message, true);
            latch.countDown();
         }
      });

      try
      {
         boolean success = latch.await(10, TimeUnit.SECONDS);
         assertTrue("Failed to notify observer", success);
      }
      catch (InterruptedException e)
      {
         assertFalse(e.getMessage(), true);
      }
		
//		 fail("Not yet implemented");
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
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public <T> T fromJSON(JsonTypeReference<T> type, String jsonPacket) throws JsonException
      {
         // TODO Auto-generated method stub
         return null;
      }
	   
	}
}
