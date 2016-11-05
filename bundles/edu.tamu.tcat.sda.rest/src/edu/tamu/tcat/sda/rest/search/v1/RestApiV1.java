package edu.tamu.tcat.sda.rest.search.v1;

import java.util.HashMap;
import java.util.Map;

public abstract class RestApiV1
{
   public static class UnifiedResult
   {
      public String query;
      public int offset;
      public int max;

      public Map<String, Object> results = new HashMap<>();
   }
}
