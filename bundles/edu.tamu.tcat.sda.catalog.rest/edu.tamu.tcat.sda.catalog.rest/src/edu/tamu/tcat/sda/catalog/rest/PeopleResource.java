package edu.tamu.tcat.sda.catalog.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
import edu.tamu.tcat.sda.catalog.people.HistoricalFigure;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalEventDV;
import edu.tamu.tcat.sda.catalog.people.dv.HistoricalFigureDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameRefDV;
import edu.tamu.tcat.sda.catalog.psql.PsqlHistoricalFigureRepo;
import edu.tamu.tcat.sda.ds.DataUpdateObserverAdapter;


@Path("/people")
public class PeopleResource
{
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<String> listPeople()
   {
      return Arrays.asList("Neal", "Jesse", "Paul", "That Other Guy");
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
      return Response.accepted().build();
   }

   
   private DbExecutor getExecutor()
   {
      String url = "jdbc:postgresql://localhost:5433/SDA";
      String user = "postgres";
      String pass = "";
      DataSourceFactory factory = new DataSourceFactory();
      final DbExecutor exec = new PsqlDbExec(factory.getDataSource(url, user, pass));
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
      
   }
}
