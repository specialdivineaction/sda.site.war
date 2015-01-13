package edu.tamu.tcat.sda.catalog.psql.idfactory;

import java.util.Map;

@Deprecated
public interface PersistenceStrategy
{
   void save(Map<String, String> data) throws PersistenceException;
   Map<String, String> load() throws PersistenceException;
}