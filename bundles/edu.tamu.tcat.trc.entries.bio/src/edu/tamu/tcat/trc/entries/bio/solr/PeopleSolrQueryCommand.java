package edu.tamu.tcat.trc.entries.bio.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.base.Joiner;

import edu.tamu.tcat.trc.entries.bio.PeopleQueryCommand;
import edu.tamu.tcat.trc.entries.bio.dv.SimplePersonDV;

public class PeopleSolrQueryCommand implements PeopleQueryCommand
{
   private final static Logger logger = Logger.getLogger(PeopleSolrQueryCommand.class.getName());

   //Solr fields
   private final static String personId = "id";
   private final static String familyName = "familyName";
   private final static String syntheticName = "syntheticName";
   private final static String displayName = "displayName";
   private final static String birthLocation = "birthLocation";
   private final static String birthDate = "birthDate";
   private final static String deathLocation = "deathLocation";
   private final static String deathDate = "deathDate";
   private final static String summary = "summary";

   private SolrQuery query = new SolrQuery();
   private Collection<String> criteria = new ArrayList<>();

   private SolrServer solr;

   public PeopleSolrQueryCommand(SolrServer solr)
   {
      this.solr = solr;
   }

   @Override
   public Collection<SimplePersonDV> getResults()
   {
      Collection<SimplePersonDV> people = new HashSet<>();
      QueryResponse response;

      try
      {
         response = solr.query(getQuery());
         SolrDocumentList results = response.getResults();

         for (SolrDocument result : results)
         {
            SimplePersonDV simplePerson = new SimplePersonDV();
            simplePerson.id = result.getFieldValue(personId).toString();
            simplePerson.syntheticName = result.getFieldValue(syntheticName).toString();
            simplePerson.familyName = (ArrayList<String>)result.getFieldValue(familyName);
            simplePerson.displayName = (ArrayList<String>)result.getFieldValue(displayName);
            simplePerson.birthLocation = result.getFieldValue(birthLocation).toString();
            simplePerson.birthDate = result.getFieldValue(birthDate).toString();
            simplePerson.deathLocation = result.getFieldValue(deathLocation).toString();
            simplePerson.deathDate = result.getFieldValue(deathDate).toString();
            simplePerson.summary = result.getFieldValue(summary).toString();

            people.add(simplePerson);
         }
      }
      catch (SolrServerException sse)
      {
         logger.log(Level.SEVERE, "The following error occurred while querying the author core :" + sse);
      }

      return people;
   }

   public SolrQuery getQuery()
   {
      String queryString = Joiner.on(" AND ").join(criteria);
      query.setQuery(queryString);
      return query;
   }

   @Override
   public PeopleQueryCommand search(String syntheticName)
   {
      criteria.add("syntheticName:\"" + syntheticName + "\"");
      return this;
   }

   @Override
   public PeopleQueryCommand byFamilyName(String familyName)
   {
      criteria.add("familyName:\"" + familyName + "\"");
      return this;
   }

   @Override
   public PeopleQueryCommand setRowLimit(int rows)
   {
      query.setRows(Integer.valueOf(rows));
      return this;
   }

}
