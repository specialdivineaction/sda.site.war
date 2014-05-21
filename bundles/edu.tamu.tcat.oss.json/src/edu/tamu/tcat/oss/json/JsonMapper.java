package edu.tamu.tcat.oss.json;

import java.io.InputStream;


public interface JsonMapper
{

   String asString(Object o) throws JsonException;

   <T> T parse(String json, Class<T> type) throws JsonException;
   
   <T> T parse(InputStream is, Class<T> type) throws JsonException;

}
