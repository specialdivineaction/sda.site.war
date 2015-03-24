package edu.tamu.tcat.trc.entries.bib.copy.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.copy.rest.v1.DigitalCopyLinkDTO;

public class PsqlDigitalCopyCreateTask implements SqlExecutor.ExecutorTask<Void>
{
   private String sql = "INSERT INTO linked_bibliographies (item_url, origin, rights_code, bibliography, active) VALUES(?,?,?,?,?)";
   private DigitalCopyLinkDTO digitalCopyLink;

   public PsqlDigitalCopyCreateTask(DigitalCopyLinkDTO dcl)
   {
      this.digitalCopyLink = dcl;
   }

   @Override
   public Void execute(Connection conn) throws Exception
   {
      try (PreparedStatement ps = conn.prepareStatement(sql))
      {
         ps.setString(1, digitalCopyLink.linkUrl);
         ps.setString(2, digitalCopyLink.origin);
         ps.setString(3, digitalCopyLink.rightsCode);
         ps.setString(4, digitalCopyLink.bibliography);
         ps.setBoolean(5, true);

         int cnt = ps.executeUpdate();
         if (cnt != 1)
            throw new ExecutionFailedException("Failed to save linked copy reference to the db. [" + digitalCopyLink.linkUrl +"]");
      }
      catch(SQLException e)
      {

      }
      return null;
   }

}
