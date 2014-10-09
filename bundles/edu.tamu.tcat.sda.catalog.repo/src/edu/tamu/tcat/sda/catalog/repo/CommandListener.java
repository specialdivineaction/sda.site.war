package edu.tamu.tcat.sda.catalog.repo;

public interface CommandListener
{
   public void onPreExecute(Command context);
   public void onPostExecute(Command context);
}
