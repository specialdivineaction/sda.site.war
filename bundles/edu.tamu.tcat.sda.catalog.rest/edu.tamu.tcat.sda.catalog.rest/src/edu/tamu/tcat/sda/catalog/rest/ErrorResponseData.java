package edu.tamu.tcat.sda.catalog.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.oss.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.sda.catalog.rest.PeopleResource.CreatePersonERD;

public class ErrorResponseData<T> 
{
   public static final String PROP_ENABLE_ERR_DETAILS = "rest.err.details.enabled";
   
   public T resource;
   public Response.Status status;
   public String message;
   public String details;
   
   public ErrorResponseData()
   {
      
   }
   
   ErrorResponseData(T resource, Response.Status status, String message, String detail)
   {
      this.resource = resource;
      this.status = status;
      this.message = message;
      this.details = detail != null ? detail : message;
   }
   
   public static Response createJsonResponse(CreatePersonERD error)
   {
      Response resp = Response.status(error.status)
                              .entity(error)
                              .type(MediaType.APPLICATION_JSON)
                              .build();
      return resp;
   }
   
   public static String getErrorDetail(Exception ex, ConfigurationProperties properties)
   {
      Boolean enableDetails = properties.getPropertyValue(PROP_ENABLE_ERR_DETAILS, Boolean.class, Boolean.valueOf(false));
      
      if (enableDetails.booleanValue())
         return null;
      
      try (StringWriter sw = new StringWriter();
           PrintWriter writer = new PrintWriter(sw))
      {
         ex.printStackTrace(writer);
         return sw.toString();
      }
      catch (Exception e) {
         String msg = "Failed to generate exception details : " + e.getMessage();
         PeopleResource.errorLogger.log(Level.SEVERE, msg, e);
         return msg;
      }
   }
}