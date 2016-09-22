package edu.tamu.tcat.sda.tasks.workflow;

import java.util.List;

public interface PriorityScheme
{
   /**
    * @return The ordered list of priorities defined by this scheme.
    */
   List<Priority> getPriories();

   public interface Priority
   {
      /**
       * @return Unique id (within a priority scheme) for this priority level.
       */
      String getId();

      /**
       * @return A label for display.
       */
      String getLabel();

      /**
       * @return A brief description of this priority.
       */
      String getDescription();

      /**
       * @return A reference to an icon to associate with this priority. This should be
       *       interpreted relative to the supplied icon strategy {@link #getIconStrategy()}.
       */
      String getIcon();

      /**
       * @return A strategy for interpreting the icon identifier. For example, URL, glyphicon,
       *       font-awesome.
       */
      String getIconStrategy();

      /**
       * @return A color to be associated with this priority.
       */
      String getColor();
   }

}
