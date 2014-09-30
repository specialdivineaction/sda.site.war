package edu.tamu.tcat.sda.catalog;

public interface CommandExecutionListener
{
   // currently a placeholder
   void notifyCommandExectution(ExecutionEvent evt) throws Exception;

   interface ExecutionEvent
   {
      Object getCommand();
   }
}
