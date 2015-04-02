package edu.tamu.tcat.trc.entries.bib.copy.hathitrust;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.Record.IdType;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.client.v1.basic.BasicRecord.BasicRecordIdentifier;
import edu.tamu.tcat.hathitrust.client.v1.basic.BibAPIClientImpl;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverStrategy;
import edu.tamu.tcat.trc.entries.bib.copy.ResourceAccessException;

public class HathiTrustCopyResolver implements CopyResolverStrategy<HathiTrustCopy>
{
   private final String identPattern = "^htid:[0-9]{9}$";
   private Pattern p;

   public HathiTrustCopyResolver()
   {
      p = Pattern.compile(identPattern);
   }

   @Override
   public boolean canResolve(String identifier)
   {
      if(identifier == null )
         return false;

      Matcher m = p.matcher(identifier.substring(0, 14));
      return  m.matches() ;

   }

   @Override
   public HathiTrustCopy resolve(String identifier) throws ResourceAccessException, IllegalArgumentException
   {
      if (!canResolve(identifier))
         throw new IllegalArgumentException("Unrecognized identifier format [" + identifier + "]");

      BibAPIClientImpl bibClient = new BibAPIClientImpl();
      // Create a pattern to get the record number our of the identifier.
      BasicRecordIdentifier recordIdent = new BasicRecordIdentifier(IdType.RECORDNUMBER, identifier.substring(5));
      bibClient.setConfig(new ConfigurationPropertiesImpl());

      try
      {
         Collection<Record> records = bibClient.lookup(recordIdent);
         Record record = records.stream().findFirst().orElse(null);

         if(record == null)
            throw new ResourceAccessException("Record not found [" + identifier +"]");
         return new HathiTrustCopy(record);

      }
      catch (HathiTrustClientException e)
      {
         throw new ResourceAccessException("A message to our readers", e);
      }
   }



   private class ConfigurationPropertiesImpl implements ConfigurationProperties
   {

      @Override
      public <T> T getPropertyValue(String name, Class<T> type) throws IllegalStateException
      {
         if (!name.equalsIgnoreCase(BibAPIClientImpl.HATHI_TRUST))
            throw new IllegalStateException("No value configured for property '" + name + "'");

         if (type != String.class)
            throw new IllegalStateException("Expected String type");

         return (T)"http://catalog.hathitrust.org/api/";
      }

      @Override
      public <T> T getPropertyValue(String name, Class<T> type, T defaultValue) throws IllegalStateException
      {
         if (!name.equalsIgnoreCase(BibAPIClientImpl.HATHI_TRUST))
            throw new IllegalStateException("No value configured for property '" + name + "'");

         if (type != String.class)
            throw new IllegalStateException("Expected String type");

         return (T)"http://catalog.hathitrust.org/api/";
      }

   }
}
