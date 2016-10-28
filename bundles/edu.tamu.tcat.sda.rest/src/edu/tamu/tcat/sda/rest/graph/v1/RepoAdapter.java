package edu.tamu.tcat.sda.rest.graph.v1;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import edu.tamu.tcat.sda.rest.graph.GraphDTO;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class RepoAdapter
{
   public static GraphDTO.Node toDTO(BiographicalEntry person, EntryResolverRegistry resolvers)
   {
      EntryId entryId = resolvers.getResolver(person).makeReference(person);

      GraphDTO.Node dto = new GraphDTO.Node();
      dto.id = resolvers.tokenize(entryId);
      dto.label = extractName(person.getCanonicalName());

      dto.metadata.put("id", entryId.getId());
      dto.metadata.put("type", entryId.getType());

      return dto;
   }

   /**
    * Converts the given work object into a graph node identified by the work id.
    * @param work
    * @return
    */
   public static GraphDTO.Node toDTO(BibliographicEntry work, EntryResolverRegistry resolvers)
   {
      EntryId entryId = resolvers.getResolver(work).makeReference(work);

      GraphDTO.Node dto = new GraphDTO.Node();

      dto.id = resolvers.tokenize(entryId);
      dto.label = extractTitle(work.getTitle());
      dto.type = "work";

      dto.metadata.put("id", entryId.getId());
      dto.metadata.put("type", entryId.getType());

      return dto;
   }

   /**
    * Since relationships can relate potentially many works, this method can produce multiple edges from a single relationship.
    * @param reln
    * @return
    */
   public static List<GraphDTO.Edge> toDTO(Relationship reln, EntryResolverRegistry resolvers)
   {
      RelationshipType type = reln.getType();

      Collection<String> relatedIds = reln.getRelatedEntities().stream()
            .map(Anchor::getTarget)
            .map(resolvers::tokenize)
            .collect(Collectors.toList());

      Collection<String> targetIds = reln.getTargetEntities().stream()
            .map(Anchor::getTarget)
            .map(resolvers::tokenize)
            .collect(Collectors.toList());

      // HACK: reverse role of source and target for analysis and display.
      //       We need to rethink the how SDA relationship types are semantically interpreted.
      //       See https://issues.citd.tamu.edu/browse/SDA-39 for more info
      return pairRelated(type, relatedIds, targetIds, (from, to) -> createEdge(to, from, reln))
            .collect(Collectors.toList());
   }

   /**
    * Encapsulates the logic for handling related pairs of entities depending on the relationship type:
    *
    *   - Undirected relationships have a collection of related entities and no target entities.
    *     Graph edges should be formed between all distinct pairs of related entities in this case.
    *
    *   - Directed relationships have a collection of related entities and a collection of target entities.
    *     Graph edges should be formed via the Cartesian product of related and target entities.
    *
    * @param type
    * @param related
    * @param targets
    * @param handler A binary function that accepts the source and target entities and outputs the desired combined edge result.
    * @return
    */
   public static <T, R> Stream<R> pairRelated(RelationshipType type, Collection<T> related, Collection<T> targets, BiFunction<T, T, R> handler)
   {
      if (related == null) {
         return Stream.empty();
      }

      Builder<R> results = Stream.builder();

      // HACK Desired structure of undirected relns: collection of related works in relatedEntities; empty targetEntities.
      //      Existing data does not have this structure, but follows the related -> target paradigm of directed relns.
      if (!type.isDirected() && related.size() > 1 && (targets == null || targets.isEmpty()))
      {
         // for every related entity, create an edge to every other entity (excluding self)
         // this creates two complementary directed edges between each pair of nodes
         for (T source : related)
         {
            for (T target : related)
            {
               if (source == target)
               {
                  continue;
               }

               results.add(handler.apply(source, target));
            }
         }
      }
      else if (targets == null)
      {
         return Stream.empty();
      }
      else
      {
         for (T source : related)
         {
            for (T target : targets)
            {
               results.add(handler.apply(source, target));

               // add complementary edge if dealing with legacy undirected data
               if (!type.isDirected())
               {
                  results.add(handler.apply(target, source));
               }
            }
         }
      }

      return results.build();
   }

   /**
    * Creates a new edge from the given source and target anchors with metadata from the given relationship.
    * This method assumes that the source and target anchors are both contained within the given relationship.
    * @param source
    * @param target
    * @param reln Provides edge metadata
    * @return
    */
   public static GraphDTO.Edge createEdge(String sourceId, String targetId, Relationship reln)
   {
      if (sourceId == null || targetId == null)
      {
         return null;
      }

      GraphDTO.Edge dto = new GraphDTO.Edge();
      dto.id = reln.getId();
      dto.source = sourceId;
      dto.target = targetId;

      RelationshipType type = reln.getType();
      dto.directed = Boolean.valueOf(type.isDirected());
      dto.relation = type.getIdentifier();
      dto.label = type.getTitle();

      dto.metadata = new HashMap<>();
      dto.metadata.put("description", reln.getDescription());

      return dto;
   }

   /**
    * Attempts to pull a display name from the given name object or {@code null} if no name can be found.
    * @param name
    * @return
    */
   private static String extractName(PersonName name)
   {
      if (name == null)
         return "Unknown";

      // look for existing display name
      String displayName = name.getDisplayName();
      if (displayName != null)
         return displayName.trim();

      // TODO perhaps move this into the display name
      // fall back to constructing a display name from given and family names
      StringJoiner sj = new StringJoiner(" ");

      String firstName = name.getGivenName();
      if (firstName != null && !firstName.trim().isEmpty())
         sj.add(firstName.trim());

      String lastName = name.getFamilyName();
      if (lastName != null && !lastName.trim().isEmpty())
         sj.add(lastName.trim());

      String constructedName = sj.toString();
      if (!constructedName.trim().isEmpty())
         return constructedName;

      return "Unknown";
   }

   /**
    * Attempt to pull a title from the given work, or {@code null} if no title can be found.
    * @param titleDefinition
    * @return
    */
   private static String extractTitle(TitleDefinition titleDefinition)
   {
      if (titleDefinition == null)
         return "Unknown";

      // HACK magic strings

      return titleDefinition.get("short").map(Optional::of)
            .orElseGet(() -> titleDefinition.get("canonical")).map(Optional::of)
            .orElseGet(() -> titleDefinition.get("bibliographic"))
            .map(Title::getFullTitle)
            .orElse("Unknown");
   }
}
