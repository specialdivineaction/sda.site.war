package edu.tamu.tcat.oss.db.psql.internal;

import java.sql.Driver;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;

/**
 * Extends {@link BasicDataSource} to correct flawed loading of the driver via class-loader.
 */
public final class BasicDataSourceExt extends BasicDataSource
{
   private final Driver driver;
   
   public BasicDataSourceExt(Driver driver)
   {
      this.driver = driver;
   }
   
   @Override
   protected ConnectionFactory createConnectionFactory() throws SQLException
   {
      //The loading of the driver via class-loader is completely utterly broken in the super.
      if (!driver.acceptsURL(getUrl()))
         return super.createConnectionFactory();
      
      if (getValidationQuery() == null)
      {
         setTestOnBorrow(false);
         setTestOnReturn(false);
         setTestWhileIdle(false);
      }

      String user = username;
      if (user != null)
         connectionProperties.put("user", user);
      else
         log("DBCP DataSource configured without a 'username'");

      String pwd = password;
      if (pwd != null)
         connectionProperties.put("password", pwd);
      else
         log("DBCP DataSource configured without a 'password'");

      ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, url, connectionProperties);
      return driverConnectionFactory;
   }
}