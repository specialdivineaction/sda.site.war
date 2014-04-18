package edu.tamu.tcat.sda.catalog.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.WorkRepository;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.ds.DataUpdateObserver;

public class PsqlWorkRepo implements WorkRepository
{
   // TODO should we use something like the data source executor service?

   @Override
   public Iterable<Work> listWorks()
   {
      // TODO Auto-generated method stub
      return null;
   }

	@Override
	public void create(WorkDV work, DataUpdateObserver<WorkDV, Work> observer) 
	{
	   
	      String sql = "INSERT INTO works (id, work) VALUES(?,?)";
	   	ObjectMapper workMap = new ObjectMapper();
	   	try
         {
	   	   Connection conn = getPostgresConn();
            String workString = workMap.writeValueAsString(work);
            
            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(workString);
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, work.id);
            ps.setObject(2, jsonObject);
            
            int complete = ps.executeUpdate();
            if (complete == 1)
               conn.close();
         }
         catch (JsonProcessingException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         catch (SQLException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
	}

	@Override
	public void update(WorkDV work, DataUpdateObserver<WorkDV, Work> observer) {
		// TODO Auto-generated method stub
		
	}
	
	private Connection getPostgresConn()
	{
		Connection con = null;

    
        String url = "jdbc:postgresql://localhost:5433/SDA";
        String user = "postgres";
        String password = "";
        
        try 
        {
           
         Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, user, password);
			
		  } 
        catch (SQLException | ClassNotFoundException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return con;
	}

}
