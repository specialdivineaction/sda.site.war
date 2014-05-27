package edu.tamu.tcat.sda.catalog.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.oss.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigureRepository;
import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;


@Path("/people")
public class PeopleResource
{
   @SuppressWarnings("unused")
   private ConfigurationProperties properties;
   private HistoricalFigureRepository repo;

   // called by DS
   public void setConfigurationProperties(ConfigurationProperties properties)
   {
      this.properties = properties;
   }
   
   // called by DS
   public void setRepository(HistoricalFigureRepository repo)
   {
      this.repo = repo;
   }
   
   // called by DS
   public void activate()
   {
      
   }
   
   // called by DS
   public void dispose()
   {
      
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<HistoricalFigureDV> listPeople()
   {
      // TODO need to add slicing/paging support
      List<HistoricalFigureDV> results = new ArrayList<HistoricalFigureDV>();
      Iterable<HistoricalFigure> people = repo.listHistoricalFigures();
      
      for (HistoricalFigure figure : people)
      {
         results.add(getHistoricalFigureDV(figure));
      }

      return Collections.unmodifiableList(results);
   }

   @GET
   @Path("{personId}")
   @Produces(MediaType.APPLICATION_JSON)
   public HistoricalFigureDV getPerson(@PathParam(value="personId") int personId)
   {
      // TODO make this a mangled string instead of an ID. Don't want people guessing 
      //      unique identifiers
      HistoricalFigure figure = repo.getPerson(personId);
      return getHistoricalFigureDV(figure);
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public HistoricalFigureDV createPerson(HistoricalFigureDV person)
   {
      CreatePersonObserver observer = new CreatePersonObserver();
      repo.create(person, observer);
      try 
      {
         HistoricalFigure createdPerson = observer.getResult();
         HistoricalFigureDV dv = getHistoricalFigureDV(createdPerson);
         return dv;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();   // HACK: remove this.
         // TODO handle exception properly.
         return null;
      }
   }

   private HistoricalFigureDV getHistoricalFigureDV(HistoricalFigure figure)
   {
      HistoricalFigureDV hfDV = new HistoricalFigureDV();
      hfDV.id = figure.getId();
      hfDV.birth = figure.getBirth();
      hfDV.death = figure.getDeath();
      
      Set<PersonName> alternativeNames = figure.getAlternativeNames();
      Set<PersonNameDV> pnDvSet = new HashSet<PersonNameDV>();
      for (PersonName name : alternativeNames)
      {
         pnDvSet.add(new PersonNameDV(name));
      }
      
      hfDV.people = pnDvSet;
      
      return hfDV;
   }

   private static final class CreatePersonObserver extends DataUpdateObserverAdapter<HistoricalFigure>
   {
      private final CountDownLatch latch;

      private volatile HistoricalFigure result;
      private volatile ResourceCreationException exception = null;
      
      CreatePersonObserver()
      {
         latch = new CountDownLatch(1);
      }

      @Override
      protected void onFinish(HistoricalFigure result)
      {
         this.result = result;
         latch.countDown();
      }
   
      @Override
      protected void onError(String message, Exception ex)
      {
         // TODO this should be a 500 error - repo could not create the resource, likely SQL 
         //      error. We should log. Possibly send message to admin.
         exception = new ResourceCreationException(message, ex);
         latch.countDown();
      }
      
      public HistoricalFigure getResult() throws Exception
      {
         // TODO need semantic exception
         
         try 
         {
            // HACK: hard coded timeout
            latch.await(10, TimeUnit.MINUTES);
         }
         catch (InterruptedException ex)
         {
            // TODO DB time out. . . . may have succeeded, client should not retry.
            // FIXME need to be able to cancel execution!
            this.cancel();
         }
         
         if (exception != null)
            throw exception;
         
         if (result == null)
            throw new IllegalStateException("Failed to obtain created person.");
         
         return result;
      }
   }
}
