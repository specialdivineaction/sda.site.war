package edu.tamu.tcat.sda.catalog.psql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.people.PeopleRepository;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.psql.tasks.PsqlCreateWorkTask;
import edu.tamu.tcat.sda.catalog.psql.tasks.PsqlListWorksTask;
import edu.tamu.tcat.sda.catalog.psql.tasks.PsqlUpdateWorksTask;
import edu.tamu.tcat.sda.catalog.psql.tasks.PsqlWorkDbTasksProvider;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.TitleDefinition;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class PsqlWorkRepo implements WorkRepository
{
   private DbExecutor exec;
   private JsonMapper jsonMapper;
   private PeopleRepository peopleRepo;
   private PsqlWorkDbTasksProvider taskProvider;

   public PsqlWorkRepo()
   {
   }

   public void setDatabaseExecutor(DbExecutor exec)
   {
      this.exec = exec;
   }

   public void setPeopleRepo(PeopleRepository repo)
   {
      this.peopleRepo = repo;
   }

   public void setJsonMapper(JsonMapper mapper)
   {
      this.jsonMapper = mapper;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(jsonMapper);

      taskProvider = new PsqlWorkDbTasksProvider();
      taskProvider.setJsonMapper(jsonMapper);
   }

   public void dispose()
   {
      this.exec = null;
      this.jsonMapper = null;
   }

   @Override
   public Person getAuthor(AuthorReference ref)
   {
      String id = ref.getId();
      try {
         // FIXME repo should accept string identifiers.
         return peopleRepo.getPerson(Integer.parseInt(id));
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Could not retrieve person instance (" + id + ").", ex);
      }
   }

   @Override
   public Iterable<Work> listWorks()
   {
      PsqlListWorksTask task = taskProvider.makeListWorksTask();

      Future<Iterable<Work>> submit = exec.submit(task);
      Iterable<Work> iterable = null;
      try
      {
         iterable = submit.get();
      }
      catch (InterruptedException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (ExecutionException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return  iterable;
   }

   @Override
   public void create(final WorkDV work, DataUpdateObserver<Work> observer)
   {
      PsqlCreateWorkTask task = taskProvider.makeCreateWorkTask(work);
      exec.submit(new ObservableTaskWrapper<>(task, observer));
   }

   @Override
   public void update(WorkDV work, DataUpdateObserver<Work> observer)
   {
      PsqlUpdateWorksTask task = taskProvider.makeUpdateWorksTask(work);
      exec.submit(new ObservableTaskWrapper<>(task, observer));
   }

   @Override
   public Iterable<Work> listWorks(String titleName)
   {
      List<Work> workResults = new ArrayList<>();
      Iterable<Work> listWorks = listWorks();
      titleName = titleName.toLowerCase();

      for (Work w : listWorks)
      {
         TitleDefinition titleDef = w.getTitle();

         for (Title t : titleDef.getAlternateTitles())
         {
            boolean titleFound = false;
            titleFound = hasTitleName(t, titleName);
            if (titleFound)
            {
               workResults.add(w);
               continue;
            }
         }

      }

      return workResults;
   }

   private boolean hasTitleName(Title title, String titleName)
   {
      String test = title.getFullTitle();
      if (test != null && test.toLowerCase().contains(titleName))
         return true;

      test = title.getTitle();
      if (test != null && test.toLowerCase().contains(titleName))
         return true;

      return false;
   }
}
