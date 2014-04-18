package edu.tamu.tcat.sda.catalog.psql;

import java.sql.Connection;

public interface DbTaskCallable<T>
{

   T execute(Connection conn) throws Exception;
}
