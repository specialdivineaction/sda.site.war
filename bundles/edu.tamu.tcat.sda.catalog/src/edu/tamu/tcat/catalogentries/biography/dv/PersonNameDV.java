package edu.tamu.tcat.catalogentries.biography.dv;

import edu.tamu.tcat.catalogentries.biography.PersonName;

public class PersonNameDV
{
   /**
    * Create a new data vehicle from the supplied {@link PersonName}.
    */
   public PersonNameDV(PersonName name)
   {
      this.title = name.getTitle();
      this.givenName = name.getGivenName();
      this.middleName = name.getMiddleName();
      this.familyName = name.getFamilyName();
      this.suffix = name.getSuffix();

      this.displayName = name.getDisplayName();
   }

   /**
    * Default constructor.
    */
   public PersonNameDV()
   {

   }

   public String title;
   public String givenName;
   public String middleName;
   public String familyName;
   public String suffix;

   public String displayName;
}
