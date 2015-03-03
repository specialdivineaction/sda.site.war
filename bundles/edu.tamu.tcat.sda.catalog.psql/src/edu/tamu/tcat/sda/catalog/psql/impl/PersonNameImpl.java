package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.catalogentries.biography.PersonName;
import edu.tamu.tcat.catalogentries.biography.dv.PersonNameDV;

public class PersonNameImpl implements PersonName
{
   private String title;
   private String givenName;
   private String middleName;
   private String familyName;
   private String suffix;

   private String displayName;


   public PersonNameImpl(PersonNameDV personDV)
   {
      title = personDV.title;
      givenName = personDV.givenName;
      middleName = personDV.middleName;
      familyName = personDV.familyName;
      suffix = personDV.suffix;

      displayName = personDV.displayName;
   }

   @Override
   public String getTitle()
   {
      return title;
   }

   @Override
   public String getGivenName()
   {
      return givenName;
   }

   @Override
   public String getMiddleName()
   {
      return middleName;
   }

   @Override
   public String getFamilyName()
   {
      return familyName;
   }

   @Override
   public String getSuffix()
   {
      return suffix;
   }

   @Override
   public String getDisplayName()
   {
      return displayName;
   }

}
