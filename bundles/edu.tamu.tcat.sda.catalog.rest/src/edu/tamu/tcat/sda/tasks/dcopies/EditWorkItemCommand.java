package edu.tamu.tcat.sda.tasks.dcopies;

public interface EditWorkItemCommand
{
   void setLabel();

   void setDescription();

   void setProperty(String key, String value);

   void clearProperty(String key);
}
