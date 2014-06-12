package edu.tamu.tcat.sda.catalog.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.oss.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigureRepository;
import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameRefDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;


@Path("/people")
public class PeopleResource
{
   @SuppressWarnings("unused")      // sets up the OSGi DS dependency if needed.
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
   public Iterable<HistoricalFigureDV> listPeople()
   {
      Iterable<HistoricalFigureDV> iterablehfDV = null;

      List<HistoricalFigureDV> listHfDV = new ArrayList<HistoricalFigureDV>();
      Iterable<HistoricalFigure> listFigures = repo.listHistoricalFigures();

      for (HistoricalFigure figure : listFigures)
      {
         listHfDV.add(getHistoricalFigureDV(figure));
      }

      iterablehfDV = listHfDV;

      return iterablehfDV;
   }

   @GET
   @Path("{personId}")
   @Produces(MediaType.APPLICATION_JSON)
   public HistoricalFigureDV getPerson(@PathParam(value="personId") int personId)
   {
      HistoricalFigure figure = repo.getPerson(personId);
      return getHistoricalFigureDV(figure);
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response createPerson(HistoricalFigureDV author)
   {
      repo.create(author, new DataUpdateObserverAdapter<HistoricalFigure>()
      {
         @Override
         protected void onFinish(HistoricalFigure result)
         {
            System.out.println("Sucess!");
         }

         @Override
         protected void onError(String message, Exception ex)
         {
            System.out.println("Error!");
         }
      });

      return Response.serverError().build();
   }

   private HistoricalFigureDV getHistoricalFigureDV(HistoricalFigure figure)
   {
      PersonNameRefDV pnDV = new PersonNameRefDV();
      HistoricalFigureDV hfDV = new HistoricalFigureDV();
      hfDV.id = figure.getId();
      hfDV.birth = figure.getBirth();
      hfDV.death = figure.getDeath();

      Set<PersonName> alternativeNames = figure.getAlternativeNames();
      Set<PersonNameRefDV> pnDvSet = new HashSet<PersonNameRefDV>();
      for (PersonName name : alternativeNames)
      {
         pnDV.title = name.getTitle();
         pnDV.displayName = name.getDisplayName();
         pnDV.givenName = name.getGivenName();
         pnDV.middleName = name.getMiddleName();
         pnDV.familyName = name.getFamilyName();
         pnDV.suffix = name.getSuffix();

         pnDvSet.add(pnDV);
      }

      hfDV.people = pnDvSet;

      return hfDV;
   }
}
