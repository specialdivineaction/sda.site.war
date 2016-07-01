package edu.tamu.tcat.sda.catalog.rest.graph.v1;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.sda.catalog.rest.graph.GraphDTO;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkSearchService;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;

public class PeopleGraphResource
{
   private final PeopleRepository peopleRepo;
   private final WorkSearchService workSearchService;

   public PeopleGraphResource(PeopleRepository peopleRepo, WorkSearchService workSearchService)
   {
      this.peopleRepo = peopleRepo;
      this.workSearchService = workSearchService;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public GraphDTO.SingleGraph getGraph()
   {
      Iterable<Person> people = () -> {
         try
         {
            return peopleRepo.listAll();
         }
         catch (CatalogRepoException e)
         {
            throw new InternalServerErrorException("unable to list people", e);
         }
      };

      GraphDTO.Graph graph = new GraphDTO.Graph();

      graph.nodes = StreamSupport.stream(people.spliterator(), true)
         .map(RepoAdapter::toDTO)
         .collect(Collectors.toList());

      return GraphDTO.SingleGraph.create(graph);
   }
}
