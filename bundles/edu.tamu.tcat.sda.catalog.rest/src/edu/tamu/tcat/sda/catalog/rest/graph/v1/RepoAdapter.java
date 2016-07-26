package edu.tamu.tcat.sda.catalog.rest.graph.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.tamu.tcat.sda.catalog.rest.graph.GraphDTO;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;

public class RepoAdapter
{
   private static final Pattern workIdPattern = Pattern.compile("^works/([^/]+)");

   public static GraphDTO.Node toDTO(Person person)
   {
      GraphDTO.Node dto = new GraphDTO.Node();

      dto.id = person.getId();
      dto.label = extractName(person.getCanonicalName());

      return dto;
   }

   /**
    * Converts the given work object into a graph node identified by the work id.
    * @param work
    * @return
    */
   public static GraphDTO.Node toDTO(Work work)
   {
      GraphDTO.Node dto = new GraphDTO.Node();

      dto.id = work.getId();
      dto.label = extractTitle(work.getTitle());
      dto.type = "work";

      return dto;
   }

   /**
    * Since relationships can relate potentially many works, this method can produce multiple edges from a single relationship.
    * @param reln
    * @return
    */
   public static List<GraphDTO.Edge> toDTO(Relationship reln)
   {
      RelationshipType type = reln.getType();

      AnchorSet relatedSet = reln.getRelatedEntities();
      Collection<Anchor> relateds = relatedSet == null ? null : relatedSet.getAnchors();

      AnchorSet targetSet = reln.getTargetEntities();
      Collection<Anchor> targets = targetSet == null ? null : targetSet.getAnchors();

      List<GraphDTO.Edge> edges = new ArrayList<>();

      // HACK Desired structure of undirected relns lists related works within the relatedEntities anchor set
      //      Existing data does not have this structure, but follows the related -> target paradigm of directed relns.
      if (!type.isDirected() && relateds != null && relateds.size() > 1 && (targets == null || targets.isEmpty()))
      {
         // for every related entity, create an edge to every other entity (excluding self)
         // this creates two complementary directed edges between each pair of nodes
         for (Anchor source : relateds)
         {
            for (Anchor target : relateds)
            {
               if (source == target)
               {
                  continue;
               }

               GraphDTO.Edge dto = createEdge(source, target, reln);
               if (dto != null)
               {
                  edges.add(dto);
               }

            }
         }
      }
      else
      {
         for (Anchor source : relateds)
         {
            for (Anchor target : targets)
            {
               // HACK: reverse role of source and target for analysis and display.
               //       We need to rethink the how SDA relationship types are semantically interpreted.
               //       See https://issues.citd.tamu.edu/browse/SDA-39 for more info
               GraphDTO.Edge dto = createEdge(target, source, reln);
               if (dto != null)
               {
                  edges.add(dto);
               }
            }
         }
      }

      return edges;
   }

   /**
    * Creates a new edge from the given source and target anchors with metadata from the given relationship.
    * This method assumes that the source and target anchors are both contained within the given relationship.
    * @param source
    * @param target
    * @param reln Provides edge metadata
    * @return
    */
   private static GraphDTO.Edge createEdge(Anchor source, Anchor target, Relationship reln)
   {

      Collection<URI> sourceUris = source.getEntryIds();
      Collection<URI> targetUris = target.getEntryIds();

      if (sourceUris == null || sourceUris.isEmpty() || targetUris == null || targetUris.isEmpty())
      {
         return null;
      }

      // HACK use first anchor entry URI
      String sourceUri = sourceUris.iterator().next().toString();
      String targetUri = targetUris.iterator().next().toString();

      // extract work ID
      Matcher sourceWorkIdMatcher = workIdPattern.matcher(sourceUri);
      Matcher targetWorkIdMatcher = workIdPattern.matcher(targetUri);
      if (!sourceWorkIdMatcher.matches() || !targetWorkIdMatcher.matches()) {
         return null;
      }

      GraphDTO.Edge dto = new GraphDTO.Edge();
      dto.id = reln.getId();
      dto.source = sourceWorkIdMatcher.group(1);
      dto.target = targetWorkIdMatcher.group(1);

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
      {
         return null;
      }

      // look for existing display name

      String displayName = name.getDisplayName();
      if (displayName != null)
      {
         return displayName.trim();
      }

      // fall back to constructing a display name from given and family names

      StringJoiner sj = new StringJoiner(" ");

      String firstName = name.getGivenName();
      if (firstName != null && !firstName.trim().isEmpty())
      {
         sj.add(firstName.trim());
      }

      String lastName = name.getFamilyName();
      if (lastName != null && !lastName.trim().isEmpty())
      {
         sj.add(lastName.trim());
      }

      String constructedName = sj.toString();
      if (!constructedName.trim().isEmpty())
      {
         return constructedName;
      }

      return null;
   }

   /**
    * Attempt to pull a title from the given work, or {@code null} if no title can be found.
    * @param titleDefinition
    * @return
    */
   private static String extractTitle(TitleDefinition titleDefinition)
   {
      if (titleDefinition == null)
      {
         return null;
      }

      Title shortTitle = titleDefinition.get("short");
      if (shortTitle != null)
      {
         return shortTitle.getFullTitle();
      }

      Title canonicalTitle = titleDefinition.get("canonical");
      if (canonicalTitle != null)
      {
         return canonicalTitle.getFullTitle();
      }

      Title bibliographicTitle = titleDefinition.get("bibliographic");
      if (bibliographicTitle != null)
      {
         return bibliographicTitle.getFullTitle();
      }

      return null;
   }
}
