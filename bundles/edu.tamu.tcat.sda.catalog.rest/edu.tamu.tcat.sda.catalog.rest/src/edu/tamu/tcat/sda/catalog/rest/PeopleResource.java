package edu.tamu.tcat.sda.catalog.rest;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.oss.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigureRepository;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;


@Path("/people")
public class PeopleResource
{
   // records internal errors accessing the REST
   private static final Logger errorLogger = Logger.getLogger("sda.catalog.rest.people");
   
   // TODO move to consts package
   
   // The time (in milliseconds) to wait for a response from the repository. Defaults to 1000.
   public static final String PROP_TIMEOUT = "rest.repo.timeout";
   
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
      CreatePersonObserver observer = new CreatePersonObserver(person);
      repo.create(person, observer);
      try 
      {
         // HACK: hard coded timeout
         HistoricalFigure createdPerson = observer.getResult(1000, TimeUnit.MILLISECONDS);
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
      return new HistoricalFigureDV(figure);
   }

   private static final class CreatePersonObserver extends DataUpdateObserverAdapter<HistoricalFigure>
   {
      private final HistoricalFigureDV person;     // for data logging purposes
      private final CountDownLatch latch;

      private volatile HistoricalFigure result;
      private volatile ResourceCreationException exception = null;

      
      CreatePersonObserver(HistoricalFigureDV person)
      {
         this.person = person;
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
      
      public HistoricalFigure getResult(long timeout, TimeUnit units) throws Exception
      {
         // TODO need semantic exception
         
         try 
         {
            latch.await(timeout, units);
         }
         catch (InterruptedException ex)
         {
            // FIXME need to be able to cancel execution!
            try  {
               this.cancel();          // prevent any further updates to the underlying database.
            }  catch (Exception e) {
               ex.addSuppressed(e);
            }
            
            // TODO log details of the user to be created
            String message = MessageFormat.format("Failed to create user {0} within alloted timeout {1} {2}", person, timeout, units);
            errorLogger.log(Level.SEVERE, message, ex);
            
            Response resp = Response.status(Response.Status.SERVICE_UNAVAILABLE)
                                    .entity(message)
                                    .type(MediaType.TEXT_PLAIN)
                                    .build();
            throw new WebApplicationException(resp);
         }
         
         if (exception != null)
            throw exception;
         
         if (result == null)
            throw new IllegalStateException("Failed to obtain created person.");
         
         return result;
      }
   }
}
