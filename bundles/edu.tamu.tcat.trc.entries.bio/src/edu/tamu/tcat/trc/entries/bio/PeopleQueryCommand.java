package edu.tamu.tcat.trc.entries.bio;

import java.util.Collection;

import edu.tamu.tcat.trc.entries.bio.dv.SimplePersonDV;

public interface PeopleQueryCommand
{
   public abstract Collection<SimplePersonDV> getResults();

   public abstract PeopleQueryCommand search(String syntheticName);

   public abstract PeopleQueryCommand byFamilyName(String familyName);

   public abstract PeopleQueryCommand setRowLimit(int rows);

}
