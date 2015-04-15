package edu.tamu.tcat.trc.entries.bib.copy.hathitrust;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.tamu.tcat.hathitrust.client.BibliographicAPIClient;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.model.BasicRecordIdentifier;
import edu.tamu.tcat.hathitrust.model.Item;
import edu.tamu.tcat.hathitrust.model.Record;
import edu.tamu.tcat.hathitrust.model.Record.IdType;
import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverStrategy;
import edu.tamu.tcat.trc.entries.bib.copy.ResourceAccessException;

public class HathiTrustCopyResolver implements CopyResolverStrategy<HathiTrustCopy>
{
   // Uses the HathiTrust SDK to construct references to DigitalCopies.
   // Initially, all we need are fairly simple links to enable users to read the book and
   // basic metadata. We can/will add additional support as needed (e.g., access to full text for indexing)

   private final Pattern copyIdPattern = Pattern.compile("^htid:(\\d{9}#(.*)$");

   private final String identPattern = "^htid:[0-9]{9}$";
   private Pattern p;
   private BibliographicAPIClient htBibliographyAPI;

   public HathiTrustCopyResolver()
   {
      p = Pattern.compile(identPattern);
   }


   public void setBibliographyAPI(BibliographicAPIClient htBibliographyAPI)
   {
      this.htBibliographyAPI = htBibliographyAPI;
   }

   public void activate()
   {

   }

   public void dispose()
   {

   }

   @Override
   public boolean canResolve(String identifier)
   {
      if(identifier == null )
         return false;

      Matcher m = p.matcher(identifier.substring(0, 14));
      return  m.matches() ;

   }

   /**
    * @param identifier Will be in the format {@code htid:<recordnumber>#itemId}.
    */
   @Override
   public HathiTrustCopy resolve(String identifier) throws ResourceAccessException, IllegalArgumentException
   {
      if (!canResolve(identifier))
         throw new IllegalArgumentException("Unrecognized identifier format [" + identifier + "]");

      Matcher matcher = copyIdPattern.matcher(identifier);
      if (!matcher.find())
         throw new IllegalArgumentException("Unrecognized identifier format [" + identifier + "]");

      String itemId = matcher.group(2);

      BasicRecordIdentifier recordId = new BasicRecordIdentifier(IdType.RECORDNUMBER, matcher.group(1));

      // Create a pattern to get the record number our of the identifier.
      try
      {
         Record record = getRecord(recordId);
         Item item = record.getItem(itemId);

         // FIXME not a copy. This is a record.
         return new HathiTrustCopy(record, item);

      }
      catch (HathiTrustClientException e)
      {
         throw new ResourceAccessException("A message to our readers", e);
      }
   }


   private Record getRecord(BasicRecordIdentifier recordId) throws HathiTrustClientException, ResourceAccessException
   {
      Collection<Record> records = htBibliographyAPI.lookup(recordId);
      Record record = records.stream().findFirst().orElse(null);
      if (record == null)
         throw new ResourceAccessException("Record not found [" + recordId +"]");
      return record;
   }
}
