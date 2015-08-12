package edu.tamu.tcat.sda.catalog.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleSearchService;

@Path("/bulk/authors")
public class AuthorList
{

   private PeopleRepository repo;
   private PeopleSearchService peopleSearchService;

   // called by DS
   public void setRepository(PeopleRepository repo)
   {
      this.repo = repo;
   }

   public void setPeopleService(PeopleSearchService service)
   {
      this.peopleSearchService = service;
   }

   // called by DS
   public void activate()
   {

   }

   // called by DS
   public void dispose()
   {

   }

   @GET
   @Produces("text/csv; charset=UTF-8")
   public String getBasicAuthorListCSV() throws CatalogRepoException
   {
      repo.listAll();
      return "";

   }
}
