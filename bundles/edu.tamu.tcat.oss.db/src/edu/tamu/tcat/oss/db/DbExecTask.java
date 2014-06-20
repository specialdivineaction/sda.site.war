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
// TODO might add Query vs Update
public interface DbExecTask<T>
{

   /**
    * Executes a specific data persistence task.
    * 
    * @param conn The data base connection to be used to run this task. 
    * @return The result of the task. This may be {@code null}.
    * @throws Exception If problems are encountered while running this task. If the task throws
    *       an exception, the implementing execution environment should attempt to rollback any 
    *       pending changes to the database. Note that the ability to rollback changes depends 
    *       on the database-specific implementation details.
    */
   public T execute(Connection conn) throws Exception;

}
