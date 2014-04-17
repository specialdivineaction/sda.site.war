package edu.tamu.tcat.sda.catalog.works;

import java.util.Locale;
import java.util.Set;

public interface TitleDefinition
{
   Title getCanonicalTitle();
   
   Title getShortTitle();
   
   Set<Title> getAlternateTitles();
   
   Title getTitle(Locale language);

}
