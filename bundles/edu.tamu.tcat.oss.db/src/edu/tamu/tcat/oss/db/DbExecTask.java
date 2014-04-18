package edu.tamu.tcat.oss.db;

import java.sql.Connection;

/**
 * <p>
 * Note that the {@link #call()} method of this class <strong>must not</strong> call foreign methods that may execute DB
 * queries. Since database access is typcially handled in a single thread, any calls that need to execute against the DB
 * will deadlock waiting while for this task to complete.
 * 
 * @param <T> The type of result returned from the {@link #call()} method.
 */
public interface DbExecTask extends Runnable
{
   // TODO might add Query vs Update
   /**
    * Called by the DB framework to supply a DB connection for use.
    * 
    * @param conn
    */
   void setConnection(Connection conn);
}
