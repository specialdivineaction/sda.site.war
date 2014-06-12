package edu.tamu.tcat.oss.json.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.oss.json.JsonTypeReference;

public class JacksonJsonMapper implements JsonMapper
{

   private ObjectMapper mapper;
   
   public JacksonJsonMapper()
   {
      
   }
   
   // called by DS
   public void activate() 
   {
      mapper = new ObjectMapper();
      // TODO load modules from plugins
   }
   
   // called by DS
   public void dispose()
   {
      mapper = null;
   }

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
      try
      {
         return mapper.readValue(is, type);
      }
      catch (IOException e)
      {
         throw new JsonException(e);
      }
   }

   @Override
   public <T> T fromJSON(String json, JsonTypeReference<T> type) throws JsonException
   {
      try
      {
         return mapper.readValue(json, new TypeRefAdapter<T>(type));
      }
      catch (IOException e)
      {
         throw new JsonException(e);
      }
   }

   @Override
   public <T> T fromJSON(InputStream is, JsonTypeReference<T> type) throws JsonException
   {
      try
      {
         return mapper.readValue(is, new TypeRefAdapter<T>(type));
      }
      catch (IOException e)
      {
         throw new JsonException(e);
      }
   }

   private final class TypeRefAdapter<T> extends TypeReference<T>
   {
      private final JsonTypeReference<T> type;
   
      private TypeRefAdapter(JsonTypeReference<T> type)
      {
         this.type = type;
      }
   
      @Override
      public Type getType()
      {
         return type.getType();
      }
   }

}
