package edu.tamu.tcat.sda.catalog.psql.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.oss.osgi.services.util.ServiceHelper;
import edu.tamu.tcat.sda.catalog.psql.PsqlWorkRepo;
import edu.tamu.tcat.sda.catalog.psql.internal.Activator;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDv;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.ds.DataUpdateObserverAdapter;


public class TestCreateWork 
{
   private static DbExecutor dbExec;
   private JsonMapper mapper;
   
   private ServiceHelper helper;

   @Before
   public void initDbConnection()
   {
      helper = new ServiceHelper(Activator.getDefault().getContext());
      
      dbExec = helper.waitForService(DbExecutor.class, 10_000);
      mapper = helper.waitForService(JsonMapper.class, 10_000);
   }
   
   @After
   public void tearDown() 
   {
      helper.close();
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
		
		PsqlWorkRepo workRepo = new PsqlWorkRepo(dbExec, mapper);
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
	}
}
