package edu.tamu.tcat.sda.catalog.works;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface TitleDefinition
{
   Title getCanonicalTitle();
   
   Title getShortTitle();
   
   Set<Title> getAlternateTitles();
   
   Title getTitle(Locale language);
   
   Title getShortForm();
   
   List<Title> getAltForm();
   
   Title getPrimary();
}
