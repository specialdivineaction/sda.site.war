package edu.tamu.tcat.sda.catalog.people.dv;

import edu.tamu.tcat.sda.catalog.people.PersonName;

public class PersonNameDV
{
   /**
    * Create a new data vehicle from the supplied {@link PersonName}.
    */
   public PersonNameDV(PersonName name)
   {
      this.title = name.getTitle();
      this.displayName = name.getDisplayName();
      this.givenName = name.getGivenName();
      this.middleName = name.getMiddleName();
      this.familyName = name.getFamilyName();
      this.suffix = name.getSuffix();
      
   }
   
   /**
    * Default constructor.
    */
   public PersonNameDV()
   {
      
   }
   
   public String title;
   public String name;
   public String givenName;
   public String middleName;
   public String familyName;
   public String suffix;
   public String displayName;
}
