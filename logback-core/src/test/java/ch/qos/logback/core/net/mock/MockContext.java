/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.net.mock; 

import java.util.List;
 
import java.util.concurrent.ExecutorService;
 

import ch.qos.logback.core.Context;
 
import ch.qos.logback.core.ContextBase;
 
import ch.qos.logback.core.status.Status;
 
import ch.qos.logback.core.status.StatusListener;
 
import ch.qos.logback.core.status.StatusManager;
 

/**
 * A mock {@link Context} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public
  class
  MockContext  extends ContextBase
 {
	

    private final ExecutorService executorService;

	

    private Status lastStatus;

	

    // START MockContext({FormalParametersInternal})//public MockContext() {
    this(new MockExecutorService());
// END MockContext({FormalParametersInternal})//  }
	

    // START MockContext(ExecutorService-ExecutorService)//public MockContext(ExecutorService executorService) {
    this.setStatusManager(new MockStatusManager());
    this.executorService = executorService;
// END MockContext(ExecutorService-ExecutorService)//  }
	

    // START getExecutorService({FormalParametersInternal})//@Override
  public ExecutorService getExecutorService() {
    return executorService;
// END getExecutorService({FormalParametersInternal})//  }
	

    // START getLastStatus({FormalParametersInternal})//public Status getLastStatus() {
    return lastStatus;
// END getLastStatus({FormalParametersInternal})//  }
	

    // START setLastStatus(Status-Status)//public void setLastStatus(Status lastStatus) {
    this.lastStatus = lastStatus;
// END setLastStatus(Status-Status)//  }
	

    private
  class
  MockStatusManager  implements StatusManager
 {
		

        // START add(Status-Status)//public void add(Status status) {
      lastStatus = status;
// END add(Status-Status)//    }
		

        // START getCopyOfStatusList({FormalParametersInternal})//public List<Status> getCopyOfStatusList() {
      throw new UnsupportedOperationException();
// END getCopyOfStatusList({FormalParametersInternal})//    }
		

        // START getCount({FormalParametersInternal})//public int getCount() {
      throw new UnsupportedOperationException();
// END getCount({FormalParametersInternal})//    }
		

        public boolean add(StatusListener listener) {
            throw new UnsupportedOperationException();
        }
		

        // START remove(StatusListener-StatusListener)//public void remove(StatusListener listener) {
      throw new UnsupportedOperationException();
// END remove(StatusListener-StatusListener)//    }
		

        // START clear({FormalParametersInternal})//public void clear() {
      throw new UnsupportedOperationException();
// END clear({FormalParametersInternal})//    }
		

        // START getCopyOfStatusListenerList({FormalParametersInternal})//public List<StatusListener> getCopyOfStatusListenerList() {
      throw new UnsupportedOperationException();
// END getCopyOfStatusListenerList({FormalParametersInternal})//    }

	}

}
