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
package ch.qos.logback.core.status; 

import ch.qos.logback.core.spi.ContextAwareBase;
 
import ch.qos.logback.core.spi.LifeCycle;
 
import ch.qos.logback.core.util.StatusPrinter;
 

import java.io.PrintStream;
 
import java.util.List;
 

/**
 *  Print all new incoming status messages on the on the designated PrintStream.
 * @author Ceki G&uuml;c&uuml;
 */
abstract
  class
  OnPrintStreamStatusListenerBase  extends ContextAwareBase
  implements StatusListener, LifeCycle
 {
	

    boolean isStarted = false;

	

    static final long DEFAULT_RETROSPECTIVE = 300;

	
  
	

    /**
     * The PrintStream used by derived classes
     * @return
     */
    abstract protected PrintStream getPrintStream();

	

    // START print(Status-Status)//private void print(Status status) {
    StringBuilder sb = new StringBuilder();
    StatusPrinter.buildStr(sb, "", status);
    getPrintStream().print(sb);
// END print(Status-Status)//  }
	

    // START addStatusEvent(Status-Status)//public void addStatusEvent(Status status) {
    if (!isStarted)
      return;
    print(status);
// END addStatusEvent(Status-Status)//  }
	

    /**
     * Print status messages retrospectively
     */
    private void retrospectivePrint() {
        if (context == null)
            return;
        long now = System.currentTimeMillis();
        StatusManager sm = context.getStatusManager();
        List<Status> statusList = sm.getCopyOfStatusList();
        for (Status status : statusList) {
            long timestampOfStatusMesage = status.getDate();
            if (isElapsedTimeLongerThanThreshold(now, timestampOfStatusMesage)) {
                print(status);
            }
        }
    }
	

    /**
     * Invoking the start method can cause the instance to print status messages created less than 
     * value of retrospectiveThresold. 
     */
    public void start() {
        isStarted = true;
        if (retrospectiveThresold > 0) {
            retrospectivePrint();
        }
    }
	

    public void setRetrospective(long retrospective) {
        this.retrospectiveThresold = retrospective;
    }
	

    public long getRetrospective() {
        return retrospectiveThresold;
    }
	

    // START stop({FormalParametersInternal})//public void stop() {
    isStarted = false;
// END stop({FormalParametersInternal})//  }
	

    // START isStarted({FormalParametersInternal})//public boolean isStarted() {
    return isStarted;
// END isStarted({FormalParametersInternal})//  }
	
    long retrospectiveThresold = DEFAULT_RETROSPECTIVE;
	

    private boolean isElapsedTimeLongerThanThreshold(long now, long timestamp) {
        long elapsedTime = now - timestamp;
        return elapsedTime < retrospectiveThresold;
    }

}
