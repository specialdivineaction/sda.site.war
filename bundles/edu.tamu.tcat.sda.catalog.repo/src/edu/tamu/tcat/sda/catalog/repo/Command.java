package edu.tamu.tcat.sda.catalog.repo;

import java.util.concurrent.Callable;

public interface Command extends Callable<Object>
{
   public void addListener(CommandListener listener);
}
