package edu.tamu.tcat.sda.tasks;

import java.util.concurrent.Future;

public interface ItemEditCommand
{

   void setLabel(String label);

   void setDescription(String description);

   void setProperty(String key, String value);

   Future<String> execute();
}
