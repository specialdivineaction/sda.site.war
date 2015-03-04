package edu.tamu.tcat.oss.json;

import java.io.InputStream;

@Deprecated // Should just use ObjectMapper directly. This seem to have no practical value
public interface JsonMapper
{

   String asString(Object o) throws JsonException;

   <T> T parse(String json, Class<T> type) throws JsonException;

   <T> T parse(InputStream is, Class<T> type) throws JsonException;

   <T> T fromJSON(String json, JsonTypeReference<T> type) throws JsonException;

   <T> T fromJSON(InputStream is, JsonTypeReference<T> type) throws JsonException;
}
