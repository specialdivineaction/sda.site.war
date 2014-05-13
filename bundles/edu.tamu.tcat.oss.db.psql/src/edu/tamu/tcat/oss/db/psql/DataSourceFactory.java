/*******************************************************************************
 * Copyright © 2008-14, All Rights Reserved
 * Texas Center for Applied Technology
 * Texas A&M Engineering Experiment Station
 * The Texas A&M University System
 * College Station, Texas, USA 77843
 *
 * Proprietary information, not for redistribution.
 ******************************************************************************/

package edu.tamu.tcat.oss.db.psql;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;

/**
 * A factory for creating {@link DataSource} instances for a particular JDBC driver. This
 * resolves errors in the implementation of driver-loading by class-loader as provided by
 * {@link BasicDataSource}. It also supports re-use of connections to the same data source
 * (as identified by the user and db url).
 *
 */
public class DataSourceFactory
{
   Map<String, BasicDataSource> dataSources = new HashMap<>();
   
   private final Driver driver;

   int maxActive = 30; 
   int maxIdle = 3; 
   int minIdle = 0; 
   int minEviction = 10 * 1000;
   int betweenEviction = 1000;
   
   public DataSourceFactory()
   {
      this.driver = new org.postgresql.Driver();
   }
   
   public DataSourceFactory(int maxActive, int maxIdle, int minIdle)
   {
      this.driver = new org.postgresql.Driver();

      this.maxActive = maxActive;
      this.maxIdle = maxIdle;
      this.minIdle = minIdle;
   }
   
   public final DataSource getDataSource(String url, String user, String pass) 
   {
      String key = user + "@" + url;
      synchronized (dataSources)
      {
         BasicDataSource dataSource = dataSources.get(key);
         if (dataSource == null) {
            dataSource = new BasicDataSourceExt(driver);
            
            dataSource.setDriverClassName(driver.getClass().getName());
            dataSource.setUsername(user);
            dataSource.setPassword(pass);
            
            dataSource.setUrl(url);
            
            dataSource.setMaxActive(maxActive);
            dataSource.setMaxIdle(maxIdle);
            dataSource.setMinIdle(minIdle);
            dataSource.setMinEvictableIdleTimeMillis(minEviction);
            dataSource.setTimeBetweenEvictionRunsMillis(betweenEviction);
            
            dataSources.put(key, dataSource);
         }
         
         return dataSource;
      }
   }
   
   /**
    * Extends {@link BasicDataSource} to correct flawed loading of the driver via class-loader.
    */
   private final static class BasicDataSourceExt extends BasicDataSource
   {
      private final Driver driver;
      
      BasicDataSourceExt(Driver driver)
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
}
