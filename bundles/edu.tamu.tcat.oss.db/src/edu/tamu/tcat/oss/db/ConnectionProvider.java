package edu.tamu.tcat.oss.db;

import java.sql.Connection;

public interface ConnectionProvider
{

   boolean isReady();

   Connection getConnection();
}
