package edu.tamu.tcat.trc.entries.bib;

import java.util.Date;
import java.util.List;

import edu.tamu.tcat.trc.entries.bib.dto.WorkInfo;


public interface WorkQueryCommand
{
   public abstract List<WorkInfo> getResults();

   public abstract WorkQueryCommand searchWorks(String title);

   public abstract WorkQueryCommand setResults(int numResults);

   public abstract WorkQueryCommand byAuthor(String authorName);

   public abstract WorkQueryCommand byPublishedDate(Date publishedDate);

   public abstract WorkQueryCommand byPublishedLocation(String location);
}
