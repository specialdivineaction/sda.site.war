package edu.tamu.tcat.sda.catalog.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.db.psql.DataSourceFactory;
import edu.tamu.tcat.oss.db.psql.PsqlDbExec;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.oss.json.JsonTypeReference;
import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameRefDV;
import edu.tamu.tcat.sda.catalog.psql.PsqlHistoricalFigureRepo;
import edu.tamu.tcat.sda.ds.DataUpdateObserverAdapter;


@Path("/people")
public class PeopleResource
{
   DbExecutor exec;
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Iterable<HistoricalFigureDV> listPeople()
   {
      Iterable<HistoricalFigureDV> iterablehfDV = null;
      Iterable<HistoricalFigure> listFigures = null;
      List<HistoricalFigureDV> listHfDV = new ArrayList<HistoricalFigureDV>();
      HistoricalFigureDV hfDV = null;
      PersonNameRefDV pnDV = new PersonNameRefDV();
//      CountDownLatch latch = new CountDownLatch(1);
      PsqlHistoricalFigureRepo repo = new PsqlHistoricalFigureRepo(getExecutor(), new SimpleJacksonMapper());
      try 
      {
         listFigures = repo.listHistoricalFigures();
         for (HistoricalFigure figure : listFigures)
         {
            hfDV = new HistoricalFigureDV();
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
            listHfDV.add(hfDV);
         }
         
         iterablehfDV = listHfDV;
         
      }
      catch (Exception e)
      {
         System.out.println("Error" + e);
      }
//      try
//      {
//         latch.await();
//      }
//      catch (InterruptedException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
      return iterablehfDV;
   }

   @GET
   @Path("{personId}")
   @Produces(MediaType.APPLICATION_JSON)
   public String getPerson(@PathParam(value="personId") int personId)
   {
	   return null;
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response createPerson(HistoricalFigureDV author)
   {
      
      PsqlHistoricalFigureRepo repo = new PsqlHistoricalFigureRepo(getExecutor(), new SimpleJacksonMapper());
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

   
   private DbExecutor getExecutor()
   {
      String url = "jdbc:postgresql://localhost:5433/SDA";
      String user = "postgres";
      String pass = "";
      DataSourceFactory factory = new DataSourceFactory();
      exec = new PsqlDbExec(factory.getDataSource(url, user, pass));
      return exec;
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
