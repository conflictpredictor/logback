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
package ch.qos.logback.core; 

import static ch.qos.logback.core.CoreConstants.CONTEXT_NAME_KEY;
 

import java.util.HashMap;
 
import java.util.Map;
 
import java.util.concurrent.ExecutorService;
 

import ch.qos.logback.core.spi.LifeCycle;
 
import ch.qos.logback.core.spi.LogbackLock;
 
import ch.qos.logback.core.status.StatusManager;
 
import ch.qos.logback.core.util.ExecutorServiceUtil;
 
import static ch.qos.logback.core.CoreConstants.FA_FILENAME_COLLISION_MAP; 
import static ch.qos.logback.core.CoreConstants.RFA_FILENAME_PATTERN_COLLISION_MAP; 

public
  class
  ContextBase  implements Context, LifeCycle
 {
	

    private long birthTime = System.currentTimeMillis();

	

    private String name;

	
    private StatusManager sm = new BasicStatusManager();

	
    // TODO propertyMap should be observable so that we can be notified
    // when it changes so that a new instance of propertyMap can be
    // serialized. For the time being, we ignore this shortcoming.
    Map<String, String> propertyMap = new HashMap<String, String>();

	
    Map<String, Object> objectMap = new HashMap<String, Object>();

	

    LogbackLock configurationLock = new LogbackLock();

	

    private volatile ExecutorService executorService;

	
    private LifeCycleManager lifeCycleManager;

	
    private boolean started;

	

    // START getStatusManager({FormalParametersInternal})//public StatusManager getStatusManager() {
    return sm;
// END getStatusManager({FormalParametersInternal})//  }
	

    /**
     * Set the {@link StatusManager} for this context. Note that by default this
     * context is initialized with a {@link BasicStatusManager}. A null value for
     * the 'statusManager' argument is not allowed.
     * <p/>
     * <p> A malicious attacker can set the status manager to a dummy instance,
     * disabling internal error reporting.
     *
     * @param statusManager the new status manager
     */
    // START setStatusManager(StatusManager-StatusManager)//public void setStatusManager(StatusManager statusManager) {
    // this method was added in response to http://jira.qos.ch/browse/LBCORE-35
    if (statusManager == null) {
      throw new IllegalArgumentException("null StatusManager not allowed");
    }
    this.sm = statusManager;
// END setStatusManager(StatusManager-StatusManager)//  }
	

    // START getCopyOfPropertyMap({FormalParametersInternal})//public Map<String, String> getCopyOfPropertyMap() {
    return new HashMap<String, String>(propertyMap);
// END getCopyOfPropertyMap({FormalParametersInternal})//  }
	

    // START putProperty(String-String-String-String)//public void putProperty(String key, String val) {
    this.propertyMap.put(key, val);
// END putProperty(String-String-String-String)//  }
	

    /**
     * Given a key, return the corresponding property value. If invoked with
     * the special key "CONTEXT_NAME", the name of the context is returned.
     *
     * @param key
     * @return
     */
    // START getProperty(String-String)//public String getProperty(String key) {
    if (CONTEXT_NAME_KEY.equals(key))
      return getName();

    return (String) this.propertyMap.get(key);
// END getProperty(String-String)//  }
	

    // START getObject(String-String)//public Object getObject(String key) {
    return objectMap.get(key);
// END getObject(String-String)//  }
	

    // START putObject(String-String-Object-Object)//public void putObject(String key, Object value) {
    objectMap.put(key, value);
// END putObject(String-String-Object-Object)//  }
	

    // START removeObject(String-String)//public void removeObject(String key) {
    objectMap.remove(key);
// END removeObject(String-String)//  }
	

    // START getName({FormalParametersInternal})//public String getName() {
    return name;
// END getName({FormalParametersInternal})//  }
	

    // START start({FormalParametersInternal})//public void start() {
    // We'd like to create the executor service here, but we can't;
    // ContextBase has not always implemented LifeCycle and there are *many*
    // uses (mostly in tests) that would need to be modified.
    started = true;
// END start({FormalParametersInternal})//  }
	

    // START stop({FormalParametersInternal})//public void stop() {
    // We don't check "started" here, because the executor service uses
    // lazy initialization, rather than being created in the start method
    stopExecutorService();
    started = false;
// END stop({FormalParametersInternal})//  }
	

    // START isStarted({FormalParametersInternal})//public boolean isStarted() {
    return started;
// END isStarted({FormalParametersInternal})//  }
	

    /**
     * Clear the internal objectMap and all properties. Removes registered
     * shutdown hook
     */
    // START reset({FormalParametersInternal})//public void reset() {
	  removeShutdownHook();
    getLifeCycleManager().reset();
    propertyMap.clear();
    objectMap.clear();
// END reset({FormalParametersInternal})//  }
	

    /**
     * The context name can be set only if it is not already set, or if the
     * current name is the default context name, namely "default", or if the
     * current name and the old name are the same.
     *
     * @throws IllegalStateException if the context already has a name, other than "default".
     */
    // START setName(String-String)//public void setName(String name) throws IllegalStateException {
    if (name != null && name.equals(this.name)) {
      return; // idempotent naming
    }
    if (this.name == null
            || CoreConstants.DEFAULT_CONTEXT_NAME.equals(this.name)) {
      this.name = name;
    } else {
      throw new IllegalStateException("Context has been already given a name");
    }
// END setName(String-String)//  }
	

    // START getBirthTime({FormalParametersInternal})//public long getBirthTime() {
    return birthTime;
// END getBirthTime({FormalParametersInternal})//  }
	

    // START getConfigurationLock({FormalParametersInternal})//public Object getConfigurationLock() {
    return configurationLock;
// END getConfigurationLock({FormalParametersInternal})//  }
	

    // START getExecutorService({FormalParametersInternal})//public ExecutorService getExecutorService() {
    if (executorService == null) {
      synchronized (this) {
        if (executorService == null) {
          executorService = ExecutorServiceUtil.newExecutorService();
        }
      }
    }
    return executorService; 
// END getExecutorService({FormalParametersInternal})//  }
	

    // START stopExecutorService({FormalParametersInternal})//private synchronized void stopExecutorService() {
    if (executorService != null) {
      ExecutorServiceUtil.shutdown(executorService);
      executorService = null;
    }
// END stopExecutorService({FormalParametersInternal})//  }
	

    // START removeShutdownHook({FormalParametersInternal})//private void removeShutdownHook() {
    Thread hook = (Thread)getObject(CoreConstants.SHUTDOWN_HOOK_THREAD);
    if(hook != null) {
      removeObject(CoreConstants.SHUTDOWN_HOOK_THREAD);
      try {
        Runtime.getRuntime().removeShutdownHook(hook);
      } catch(IllegalStateException e) {
        //if JVM is already shutting down, ISE is thrown
        //no need to do anything else
      }
    }
// END removeShutdownHook({FormalParametersInternal})//  }
	

    // START register(LifeCycle-LifeCycle)//public void register(LifeCycle component) {
    getLifeCycleManager().register(component);
// END register(LifeCycle-LifeCycle)//  }
	

    /**
     * Gets the life cycle manager for this context.
     * <p>
     * The default implementation lazily initializes an instance of
     * {@link LifeCycleManager}.  Subclasses may override to provide a custom 
     * manager implementation, but must take care to return the same manager
     * object for each call to this method.
     * <p>
     * This is exposed primarily to support instrumentation for unit testing.
     * 
     * @return manager object 
     */
    // START getLifeCycleManager({FormalParametersInternal})//synchronized LifeCycleManager getLifeCycleManager() {
    if (lifeCycleManager == null) {
      lifeCycleManager = new LifeCycleManager();
    }
    return lifeCycleManager;
// END getLifeCycleManager({FormalParametersInternal})//  }
	

    // START toString({FormalParametersInternal})//@Override
  public String toString() {
    return name;
// END toString({FormalParametersInternal})//  }
	

    public ContextBase() {
        initCollisionMaps();
    }
	

    protected void initCollisionMaps() {
        putObject(FA_FILENAME_COLLISION_MAP, new HashMap<String, String>());
        putObject(RFA_FILENAME_PATTERN_COLLISION_MAP, new HashMap<String, String>());
    }

}
