package edu.tamu.tcat.sda.catalog.psql.test.PsqlTasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public class CleanIdTableDBTask implements SqlExecutor.ExecutorTask<Void>
{
   final String cleanRelationships = "DELETE FROM id_table";

   public CleanIdTableDBTask()
   {
   }

   @Override
   public Void execute(Connection conn) throws Exception
   {
      try (PreparedStatement ps = conn.prepareStatement(cleanRelationships))
      {
         ps.executeUpdate();
      }
      catch (SQLException e)
      {
         throw new SQLException("No records to delete." + e);
      }

      return null;
   }

}
