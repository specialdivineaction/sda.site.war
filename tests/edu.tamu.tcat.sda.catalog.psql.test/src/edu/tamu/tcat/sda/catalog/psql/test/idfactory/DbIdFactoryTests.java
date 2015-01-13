package edu.tamu.tcat.sda.catalog.psql.test.idfactory;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.osgi.services.util.ServiceHelper;
import edu.tamu.tcat.sda.catalog.IdFactory;
import edu.tamu.tcat.sda.catalog.psql.internal.Activator;

public class DbIdFactoryTests
{

   public DbIdFactoryTests()
   {
      // TODO Auto-generated constructor stub
   }

   @BeforeClass
   public static void setup()
   {

   }

   @AfterClass
   public static void tearDown()
   {

   }

   @Test
   public void getIdFactory()
   {
      try (ServiceHelper helper = new ServiceHelper(Activator.getDefault().getContext()))
      {
         IdFactory idFactory = helper.waitForService(IdFactory.class, 10_000);
         assertNotNull("Failed to retrieve id factory", idFactory);
      }
   }
}
