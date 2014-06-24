package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.sda.catalog.people.PersonName;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameDV;

public class PersonNameImpl implements PersonName
{
   // TODO change to local private final fields
   PersonNameDV personRef;
   public PersonNameImpl(PersonNameDV personDV)
   {
      this.personRef = personDV;
   }
   
   @Override
   public String getTitle()
   {
      return personRef.title;
   }

   @Override
   public String getGivenName()
   {
      return personRef.givenName;
   }

   @Override
   public String getMiddleName()
   {
      return personRef.middleName;
   }

   @Override
   public String getFamilyName()
   {
      return personRef.familyName;
   }

   @Override
   public String getSuffix()
   {
      return personRef.suffix;
   }

   @Override
   public String getDisplayName()
   {
      return personRef.displayName;
   }

}
