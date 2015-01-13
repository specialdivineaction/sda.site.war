package edu.tamu.tcat.sda.catalog.idfactory.impl.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

/**
 * Retrieves a new ID grant from the database and updates the DB to ensure that the
 * next range of IDs doesn't overlap.
 */
public class GetIdGrantTask implements SqlExecutor.ExecutorTask<IdGrant>
{
   private static final int MAX_ATTEMPTS = 5;     // HACK: need to retrieve from a

   private static final String QUERY = "SELECT next_id FROM id_table WHERE context = ?";
   private static final String UPDATE = "UPDATE id_table SET next_id = ? WHERE context = ? AND next_id = ?";
   private static final String CREATE = "INSERT INTO  id_table (next_id, context) VALUES (?, ?)";

   private final String context;
   private final long increment;

   private long initial;
   private long limit;
   private boolean isNewContext;

   public GetIdGrantTask(String context, long increment)
   {
      this.context = context;
      this.increment = increment;
   }

   @Override
   public IdGrant execute(Connection conn)
   {
      int attempts = 0;
      do {
         if (attempts > MAX_ATTEMPTS)
            throw new IllegalStateException("Failed to retrieve id from database after " + attempts + " tries.");

         attempts++;
         getNextIdSequence(conn);
      } while (!updateIdTable(conn));

      return new IdGrant(context, initial, limit);
   }

   private void getNextIdSequence(Connection conn)
   {
      try (PreparedStatement stmt = conn.prepareStatement(QUERY))
      {
         stmt.setString(1, context);
         try (ResultSet rs = stmt.executeQuery())
         {
            if (!rs.next())
            {
               initial = 1;
               limit = initial + increment;
               isNewContext = true;
            }
            else
            {
               initial = rs.getLong("next_id");
               limit = initial + increment;
               isNewContext = false;
            }
         }
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Failed to query id table", e);
      }
   }

   private boolean updateIdTable(Connection conn)
   {
      return (isNewContext) ?  insertLimit(conn) : updateLimit(conn);
   }

   private boolean updateLimit(Connection conn)
   {
      try (PreparedStatement stmt = conn.prepareStatement(UPDATE))
      {
         stmt.setLong(1, limit + 1);
         stmt.setString(2, context);
         stmt.setLong(3, initial);

         int ct = stmt.executeUpdate();
         return ct == 1;
      }
      catch (SQLException e)
      {
         return false;
      }
   }

   private boolean insertLimit(Connection conn)
   {
      try (PreparedStatement stmt = conn.prepareStatement(CREATE))
      {
         stmt.setLong(1, limit + 1);
         stmt.setString(2, context);

         int ct = stmt.executeUpdate();
         return ct == 1;
      }
      catch (SQLException e)
      {
         return false;
      }
   }
}