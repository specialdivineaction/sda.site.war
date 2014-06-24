package edu.tamu.tcat.sda.catalog.rest;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.oss.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.sda.catalog.people.PeopleRepository;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;


@Path("/people")
public class PeopleResource
{
   // TODO add authentication filter in front of this call

   // records internal errors accessing the REST
   static final Logger errorLogger = Logger.getLogger("sda.catalog.rest.people");


   // TODO move to consts class
   // The time (in milliseconds) to wait for a response from the repository. Defaults to 1000.
   public static final String PROP_TIMEOUT = "rest.repo.timeout";
   public static final String PROP_TIMEOUT_UNITS = "rest.repo.timeout.units";

   public static final String PROP_ENABLE_ERR_DETAILS = "rest.err.details.enabled";

   private ConfigurationProperties properties;
   private PeopleRepository repo;

   // called by DS
   public void setConfigurationProperties(ConfigurationProperties properties)
   {
      this.properties = properties;
   }

   // called by DS
   public void setRepository(PeopleRepository repo)
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
   public List<PersonDV> listPeople()
   {
      // TODO need to add slicing/paging support
      List<PersonDV> results = new ArrayList<PersonDV>();
      Iterable<Person> people = repo.listHistoricalFigures();

      for (Person figure : people)
      {
         results.add(getHistoricalFigureDV(figure));
      }

      return Collections.unmodifiableList(results);
   }

   /**
    * <api_endpoint>/people?q=query
    * @param query
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<PersonDV> find(@QueryParam(value="q") String query)
   {
      throw new UnsupportedOperationException();
   }

   @GET
   @Path("{personId}")
   @Produces(MediaType.APPLICATION_JSON)
   public PersonDV getPerson(@PathParam(value="personId") long personId)
   {
      // FIXME make this a string based identifier
      // TODO make this a mangled string instead of an ID. Don't want people guessing
      //      unique identifiers
      Person figure = repo.getPerson(personId);
      return getHistoricalFigureDV(figure);
   }

   @DELETE
   @Path("{personId}")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response deletePerson(@PathParam(value="personId") String personId) throws Exception
   {
      int timeout = properties.getPropertyValue(PROP_TIMEOUT, Integer.class, Integer.valueOf(1000)).intValue();
      String u = properties.getPropertyValue(PROP_TIMEOUT_UNITS, String.class, TimeUnit.MILLISECONDS.toString());
      TimeUnit units = TimeUnit.valueOf(u);

      DeletePersonObserver observer = new DeletePersonObserver();
      repo.delete(personId, observer);

      try
      {
         observer.getResult(timeout, units);
         return Response.noContent().build();
      }
      catch (InterruptedException iex)
      {
         DeletePersonERD error = DeletePersonERD.create(personId, iex, properties, timeout, units);
         errorLogger.log(Level.SEVERE, error.message, iex);
         throw new WebApplicationException(ErrorResponseData.createJsonResponse(error));
      }
      catch (ResourceCreationException rce)
      {
         DeletePersonERD error = DeletePersonERD.create(personId, rce, properties);
         errorLogger.log(Level.SEVERE, error.message, rce);
         throw new WebApplicationException(ErrorResponseData.createJsonResponse(error));
      }
      catch (Exception ex)
      {
         DeletePersonERD error = DeletePersonERD.create(personId, ex, properties);
         errorLogger.log(Level.SEVERE, error.message, ex);
         throw new WebApplicationException(ErrorResponseData.createJsonResponse(error));
      }
   }

   @PUT
   @Path("{personId}")
   @Consumes(MediaType.APPLICATION_JSON)
   public String updatePerson(PersonDV person) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public PersonDV createPerson(PersonDV person) throws Exception
   {
      int timeout = properties.getPropertyValue(PROP_TIMEOUT, Integer.class, Integer.valueOf(1000)).intValue();
      String u = properties.getPropertyValue(PROP_TIMEOUT_UNITS, String.class, TimeUnit.MILLISECONDS.toString());
      TimeUnit units = TimeUnit.valueOf(u);

      CreatePersonObserver observer = new CreatePersonObserver();
      repo.create(person, observer);

      try
      {
         Person createdPerson = observer.getResult(timeout, units);
         PersonDV dv = getHistoricalFigureDV(createdPerson);
         return dv;
      }
      catch (InterruptedException iex)
      {
         CreatePersonERD error = CreatePersonERD.create(person, iex, properties, timeout, units);
         errorLogger.log(Level.SEVERE, error.message, iex);
         throw new WebApplicationException(ErrorResponseData.createJsonResponse(error));
      }
      catch (ResourceCreationException rce)
      {
         CreatePersonERD error = CreatePersonERD.create(person, rce, properties);
         errorLogger.log(Level.SEVERE, error.message, rce);
         throw new WebApplicationException(ErrorResponseData.createJsonResponse(error));
      }
      catch (Exception ex)
      {
         CreatePersonERD error = CreatePersonERD.create(person, ex, properties);
         errorLogger.log(Level.SEVERE, error.message, ex);
         throw new WebApplicationException(ErrorResponseData.createJsonResponse(error));
      }
   }

   private PersonDV getHistoricalFigureDV(Person figure)
   {
      return new PersonDV(figure);
   }

   private static final class CreatePersonObserver extends DataUpdateObserverAdapter<Person>
   {
      private final CountDownLatch latch;

      private volatile Person result;
      private volatile ResourceCreationException exception = null;

      CreatePersonObserver()
      {
         this.latch = new CountDownLatch(1);
      }

      @Override
      @SuppressWarnings("hiding")
      protected void onFinish(Person result)
      {
         this.result = result;
         this.latch.countDown();
      }

      @Override
      protected void onError(String message, Exception ex)
      {
         exception = new ResourceCreationException(message, ex);
         latch.countDown();
      }

      public Person getResult(long timeout, TimeUnit units) throws InterruptedException, ResourceCreationException
      {
         latch.await(timeout, units);

         if (exception != null)
            throw exception;

         Objects.requireNonNull(result, "Repository failed to return created person");
         return result;
      }
   }

   private static final class DeletePersonObserver extends DataUpdateObserverAdapter<Void>
   {
      private final CountDownLatch latch;

      private volatile ResourceCreationException exception = null;

      DeletePersonObserver()
      {
         this.latch = new CountDownLatch(1);
      }

      @Override
      protected void onFinish(Void result)
      {
         this.latch.countDown();
      }

      @Override
      protected void onError(String message, Exception ex)
      {
         exception = new ResourceCreationException(message, ex);
         latch.countDown();
      }

      public Void getResult(long timeout, TimeUnit units) throws InterruptedException, ResourceCreationException
      {
         latch.await(timeout, units);

         if (exception != null)
            throw exception;

         return null;
      }
   }

   public static class CreatePersonERD extends ErrorResponseData<PersonDV>
   {

      public CreatePersonERD()
      {
         super();
      }

      private CreatePersonERD(PersonDV person, Response.Status status, String message, String detail)
      {
         super(person, status, message, detail);
      }

      /**
       * Constructs an error response object in the event that the request times out waiting
       * on the repository.
       */
      public static CreatePersonERD create(
            PersonDV person, InterruptedException iex, ConfigurationProperties properties,
            int timeout, TimeUnit units)
      {
         String message = MessageFormat.format("Failed to create person within alloted timeout {1} {2}",
               Integer.valueOf(timeout), units);

         String detail = ErrorResponseData.getErrorDetail(iex, properties);
         return new CreatePersonERD(person, Response.Status.SERVICE_UNAVAILABLE, message, detail);
      }

      /**
       * Constructs an error response object in the event that the repository throws an
       * exception while create the person.
       */
      public static CreatePersonERD create(
            PersonDV person, ResourceCreationException ex, ConfigurationProperties properties)
      {
         String message = "Failed to create a new person.";
         String detail = ErrorResponseData.getErrorDetail(ex, properties);
         return new CreatePersonERD(person, Response.Status.INTERNAL_SERVER_ERROR, message, detail);
      }

      /**
       * Constructs an error response object in the event that the repository throws an
       * exception while create the person.
       */
      public static CreatePersonERD create(PersonDV person, Exception ex, ConfigurationProperties properties)
      {
         String message = "Unexpected error attempting to create a new person.";
         String detail = ErrorResponseData.getErrorDetail(ex, properties);
         return new CreatePersonERD(person, Response.Status.INTERNAL_SERVER_ERROR, message, detail);
      }
   }

   public static class DeletePersonERD extends ErrorResponseData<String>
   {

      public DeletePersonERD()
      {
         super();
      }

      private DeletePersonERD(String personId, Response.Status status, String message, String detail)
      {
         super(personId, status, message, detail);
      }

      /**
       * Constructs an error response object in the event that the request times out waiting
       * on the repository.
       */
      public static DeletePersonERD create(
            String personId, InterruptedException iex, ConfigurationProperties properties,
            int timeout, TimeUnit units)
      {
         String message = MessageFormat.format("Failed to delete person {{1}} within alloted timeout {2} {3}",
               personId, Integer.valueOf(timeout), units);

         String detail = ErrorResponseData.getErrorDetail(iex, properties);
         return new DeletePersonERD(personId, Response.Status.SERVICE_UNAVAILABLE, message, detail);
      }

      /**
       * Constructs an error response object in the event that the repository throws an
       * exception while deleting the person.
       */
      public static DeletePersonERD create(
            String personId, ResourceCreationException ex, ConfigurationProperties properties)
      {
         String message = MessageFormat.format("Failed to delete person ({1}).", personId);

         String detail = ErrorResponseData.getErrorDetail(ex, properties);
         return new DeletePersonERD(personId, Response.Status.INTERNAL_SERVER_ERROR, message, detail);
      }

      /**
       * Constructs an error response object in the event that the repository throws an
       * exception while deleting the person.
       */
      public static DeletePersonERD create(String personId, Exception ex, ConfigurationProperties properties)
      {
         String message = MessageFormat.format("Unexpected error attempting to delete a person ({1}).", personId);
         String detail = ErrorResponseData.getErrorDetail(ex, properties);
         return new DeletePersonERD(personId, Response.Status.INTERNAL_SERVER_ERROR, message, detail);
      }
   }

}
