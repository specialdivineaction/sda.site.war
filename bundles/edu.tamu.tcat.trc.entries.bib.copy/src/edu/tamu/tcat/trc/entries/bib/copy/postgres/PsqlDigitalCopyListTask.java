package edu.tamu.tcat.trc.entries.bib.copy.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.copy.DigitalCopyLink;
import edu.tamu.tcat.trc.entries.bib.copy.rest.v1.DigitalCopyLinkDTO;

public class PsqlDigitalCopyListTask implements SqlExecutor.ExecutorTask<Iterable<DigitalCopyLink>>
{
   private String sql = "SELECT * FROM linked_bibliographies WHERE bibliography = ?";
   private String bibliographyUrl;

   public PsqlDigitalCopyListTask()
   {
   }

   public PsqlDigitalCopyListTask(String bibliographyUrl)
   {
      this.bibliographyUrl = bibliographyUrl;
   }

   @Override
   public Iterable<DigitalCopyLink> execute(Connection conn) throws Exception
   {
      Collection<DigitalCopyLink> links = new ArrayList<>();
      try (PreparedStatement ps = conn.prepareStatement(sql))
      {
         if(!bibliographyUrl.isEmpty())
            ps.setString(1, bibliographyUrl);
         else
            ps.setString(1, "");

         ResultSet rs = ps.executeQuery();

         while(rs.next())
         {
            DigitalCopyLinkDTO dcl = new DigitalCopyLinkDTO();
            dcl.linkUrl = rs.getString("item_url");
            dcl.bibliography = rs.getString("bibliography");
            dcl.origin = rs.getString("origin");
            dcl.rightsCode = rs.getString("rights_code");

            links.add(new DigitalCopyLinkImpl(dcl));
         }
      }
      return links;
   }

}
