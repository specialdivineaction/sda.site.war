package edu.tamu.tcat.oss.json;


public interface JsonMapper
{

   String asString(Object o) throws JsonException;

   <T> T parse(String json, Class<T> type) throws JsonException;

}
