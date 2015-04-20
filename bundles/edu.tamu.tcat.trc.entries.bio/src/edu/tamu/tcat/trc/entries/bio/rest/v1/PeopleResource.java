package edu.tamu.tcat.trc.entries.bio.rest.v1;

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

import edu.tamu.tcat.catalogentries.CatalogRepoException;
import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.bio.EditPeopleCommand;
import edu.tamu.tcat.trc.entries.bio.PeopleRepository;
import edu.tamu.tcat.trc.entries.bio.Person;
import edu.tamu.tcat.trc.entries.bio.dv.PersonDV;
import edu.tamu.tcat.trc.entries.bio.rest.v1.model.PersonId;
import edu.tamu.tcat.trc.entries.bio.rest.v1.model.SimplePersonResultDV;


@Path("/people")
public class PeopleResource
{
   // TODO add authentication filter in front of this call
   // TODO create PersonResource

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
   public List<SimplePersonResultDV> listPeople(@QueryParam(value="syntheticName") String prefix, @QueryParam(value="numResults") int numResults)
   {
      try {
         List<SimplePersonResultDV> results = new ArrayList<>();

         Iterable<Person> people = (prefix == null) ? repo.findPeople() : repo.findByName(prefix);
         for (Person person : people) {
            results.add(new SimplePersonResultDV(person));

            if (results.size() == numResults) {
               break;
            }
         }

         return results;
      }
      catch (CatalogRepoException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return Collections.emptyList();
      }

      /*
      MultivaluedMap<String, String> queryParams = ctx.getQueryParameters();
      AuthorController controller = new AuthorController();
      // TODO need to add slicing/paging support
      // TODO add mappers for exceptions. CatalogRepoException should map to internal error.

      List<SimplePersonDV> results = new ArrayList<SimplePersonDV>();
      results = controller.query(queryParams);
      return Collections.unmodifiableList(results);
      */
   }

   @GET
   @Path("{personId}")
   @Produces(MediaType.APPLICATION_JSON)
   public PersonDV getPerson(@PathParam(value="personId") String personId) throws NoSuchCatalogRecordException
   {
      // FIXME make this a string based identifier
      // TODO make this a mangled string instead of an ID. Don't want people guessing
      //      unique identifiers
      // TODO add mappers for exceptions.
      //       CatalogRepoException should map to internal error.
      //       NoSuchCatalogRecordException should map to 404
      Person figure = repo.get(personId);
      return getHistoricalFigureDV(figure);
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public PersonId createPerson(PersonDV person) throws Exception
   {
      PersonId personId = new PersonId();
      EditPeopleCommand createCommand = repo.create();

      createCommand.setAll(person);
      String id = createCommand.execute().get();
      personId.id = id;
      return personId;
   }

   @PUT
   @Path("{personId}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public PersonId updatePerson(PersonDV person) throws Exception
   {
      PersonId personId = new PersonId();
      EditPeopleCommand updateCommand = repo.update(person);
      updateCommand.execute().get();
      personId.id = person.id;
      return personId;
   }

   @DELETE
   @Path("{personId}")
   @Consumes(MediaType.APPLICATION_JSON)
   public void deletePerson(@PathParam(value="personId") String personId) throws Exception
   {
      EditPeopleCommand deleteCommand = repo.delete(personId);
      deleteCommand.execute();
   }

   private PersonDV getHistoricalFigureDV(Person figure)
   {
      return new PersonDV(figure);
   }
}
