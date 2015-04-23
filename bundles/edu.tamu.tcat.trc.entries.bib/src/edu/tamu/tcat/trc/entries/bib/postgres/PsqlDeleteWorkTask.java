package edu.tamu.tcat.trc.entries.bib.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;

@Deprecated // no need to provide separate impl
public class PsqlDeleteWorkTask implements SqlExecutor.ExecutorTask<String>
{
   private final static String deleteSql = "UPDATE works SET active = false WHERE id = ?";
   private WorkDV work;

   public PsqlDeleteWorkTask(WorkDV work)
   {
      this.work = work;
   }

   @Override
   public String execute(Connection conn) throws Exception
   {
      try (PreparedStatement ps = conn.prepareCall(deleteSql))
      {
         ps.setString(1, work.id);

         int ct = ps.executeUpdate();
         if (ct != 1)
            throw new ExecutionFailedException("Failed to de-activate work, id: [" + work.id + "]. Unexpected number of rows updated [" + ct + "]");
      }
      catch(SQLException e)
      {
         throw new IllegalStateException("Failed to de-activate work: [" + work.id + "]");
      }
      return work.id;
   }

}
