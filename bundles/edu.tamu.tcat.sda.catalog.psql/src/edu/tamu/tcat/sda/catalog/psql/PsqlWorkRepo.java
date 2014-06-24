package edu.tamu.tcat.sda.catalog.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.oss.db.DbExecTask;
import edu.tamu.tcat.oss.db.DbExecutor;
import edu.tamu.tcat.oss.db.ExecutionFailedException;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.people.PeopleRepository;
import edu.tamu.tcat.sda.catalog.people.Person;
import edu.tamu.tcat.sda.catalog.psql.impl.WorkImpl;
import edu.tamu.tcat.sda.catalog.works.AuthorReference;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class PsqlWorkRepo implements WorkRepository
{
   private final static class PsqlCreateWorkTask implements DbExecTask<Work>
   {
      private final static String sql = "INSERT INTO works (work) VALUES(?)";

      private final WorkDV work;
      private final JsonMapper jsonMapper;

      private PsqlCreateWorkTask(WorkDV work, JsonMapper jsonMapper)
      {
         // TODO convert to form where these can be configured using plugins/task provider, etc.
         this.work = work;
         this.jsonMapper = jsonMapper;
      }

      private String getJson()
      {
         try
         {
            return jsonMapper.asString(work);
         }
         catch (JsonException jpe)
         {
            throw new IllegalArgumentException("Failed to serialize the supplied work [" + work + "]", jpe);
         }
      }

      @Override
      public Work execute(Connection conn) throws SQLException, ExecutionFailedException
      {
         try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
         {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(getJson());

            ps.setObject(1, jsonObject);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new ExecutionFailedException("Failed to create work. Unexpected number of rows updates [" + ct + "]");

            try (ResultSet rs = ps.getGeneratedKeys())
            {
               if (!rs.next())
                  throw new ExecutionFailedException("Failed to generate id for a work [" + work + "]");
               work.id = Integer.toString(rs.getInt("id"));
            }

            return new WorkImpl(work);
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Failed to create work: [" + work + "]");
         }
      }
   }

   private DbExecutor exec;
   private JsonMapper jsonMapper;
   private PeopleRepository peopleRepo;

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
      final String sqlQuery = "SELECT work FROM works";
      DbExecTask<Iterable<Work>> query = new DbExecTask<Iterable<Work>>()
            {

               @Override
               public Iterable<Work> execute(Connection conn) throws Exception
               {
                  List<Work> events = new ArrayList<>();
                  Iterable<Work> eIterable = new ArrayList<>();
                  try (PreparedStatement ps = conn.prepareStatement(sqlQuery);
                       ResultSet rs = ps.executeQuery())
                  {
                     PGobject pgo = new PGobject();

                     while(rs.next())
                     {
                        Object object = rs.getObject("work");
                        if (object instanceof PGobject)
                           pgo = (PGobject)object;
                        else
                           System.out.println("Error!");

                        WorkDV parse = jsonMapper.parse(pgo.toString(), WorkDV.class);
                        WorkImpl figureRef = new WorkImpl(parse);
                        try
                        {
                           events.add(figureRef);
                        }
                        catch(Exception e)
                        {
                           System.out.println();
                        }
                     }
                  }
                  catch (Exception e)
                  {
                     System.out.println("Error" + e);
                  }
                  eIterable = events;
                  return eIterable;
               }

            };

            Future<Iterable<Work>> submit = exec.submit(query);
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
      DbExecTask<Work> task = new PsqlCreateWorkTask(work, jsonMapper);
      exec.submit(new ObservableTaskWrapper<>(task, observer));
   }

   @Override
   public void update(WorkDV work, DataUpdateObserver<Work> observer)
   {
      // TODO Auto-generated method stub

   }
}
