/*******************************************************************************
 * Copyright © 2008-14, All Rights Reserved
 * Texas Center for Applied Technology
 * Texas A&M Engineering Experiment Station
 * The Texas A&M University System
 * College Station, Texas, USA 77843
 *
 * Proprietary information, not for redistribution.
 ******************************************************************************/

package edu.tamu.tcat.oss.db;

public interface DatabaseExecutionListener<T>
{
   void finished(T result);
   
   void failed(Exception ex);
}
