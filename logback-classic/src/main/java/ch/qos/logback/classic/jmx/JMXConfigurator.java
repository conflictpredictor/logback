/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2016, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.jmx; 

import java.io.File;
 
import java.io.FileNotFoundException;
 
import java.net.MalformedURLException;
 
import java.net.URL;
 
import java.util.ArrayList;
 
import java.util.Iterator;
 
import java.util.List;
 

import javax.management.InstanceNotFoundException;
 
import javax.management.MBeanRegistrationException;
 
import javax.management.MBeanServer;
 
import javax.management.ObjectName;
 

import ch.qos.logback.classic.Level;
 
import ch.qos.logback.classic.Logger;
 
import ch.qos.logback.classic.LoggerContext;
 
import ch.qos.logback.classic.joran.JoranConfigurator;
 
import ch.qos.logback.classic.spi.LoggerContextListener;
 
import ch.qos.logback.classic.util.ContextInitializer;
 
import ch.qos.logback.core.joran.spi.JoranException;
 
import ch.qos.logback.core.spi.ContextAwareBase;
 
import ch.qos.logback.core.status.Status;
 
import ch.qos.logback.core.status.StatusListener;
 
import ch.qos.logback.core.status.StatusListenerAsList;
 
import ch.qos.logback.core.status.StatusManager;
 
import ch.qos.logback.core.util.StatusPrinter;
 

/**
 * A class that provides access to logback components via JMX.
 * 
 * <p>Since this class implements {@link JMXConfiguratorMBean} it has to be
 * named as JMXConfigurator}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 * Contributor: Sebastian Davids See http://bugzilla.qos.ch/show_bug.cgi?id=35
 */
public
  class
  JMXConfigurator  extends ContextAwareBase
  implements JMXConfiguratorMBean, LoggerContextListener
 {
	

    private static String EMPTY = "";

	

    LoggerContext loggerContext;

	
    MBeanServer mbs;

	
    ObjectName objectName;

	
    String objectNameAsString;

	

    // whether to output status events on the console when reloading the
    // configuration
    boolean debug = true;

	

    boolean started;

	

    // START JMXConfigurator(LoggerContext-LoggerContext-MBeanServer-MBeanServer-ObjectName-ObjectName)//public JMXConfigurator(LoggerContext loggerContext, MBeanServer mbs,
      ObjectName objectName) {
    started = true;
    this.context = loggerContext;
    this.loggerContext = loggerContext;
    this.mbs = mbs;
    this.objectName = objectName;
    this.objectNameAsString = objectName.toString();
    if (previouslyRegisteredListenerWithSameObjectName()) {
      addError("Previously registered JMXConfigurator named ["
          + objectNameAsString + "] in the logger context named ["
          + loggerContext.getName() + "]");
    } else {
      // register as a listener only if there are no homonyms
      loggerContext.addListener(this);
    }
// END JMXConfigurator(LoggerContext-LoggerContext-MBeanServer-MBeanServer-ObjectName-ObjectName)//  }
	

    // START previouslyRegisteredListenerWithSameObjectName({FormalParametersInternal})//private boolean previouslyRegisteredListenerWithSameObjectName() {
    List<LoggerContextListener> lcll = loggerContext.getCopyOfListenerList();
    for (LoggerContextListener lcl : lcll) {
      if (lcl instanceof JMXConfigurator) {
        JMXConfigurator jmxConfigurator = (JMXConfigurator) lcl;
        if (objectName.equals(jmxConfigurator.objectName)) {
          return true;
        }
      }
    }
    return false;
// END previouslyRegisteredListenerWithSameObjectName({FormalParametersInternal})//  }
	

    // START reloadDefaultConfiguration({FormalParametersInternal})//public void reloadDefaultConfiguration() throws JoranException {
    ContextInitializer ci = new ContextInitializer(loggerContext);
    URL url = ci.findURLOfDefaultConfigurationFile(true);
    reloadByURL(url);
// END reloadDefaultConfiguration({FormalParametersInternal})//  }
	

    // START reloadByFileName(String-String)//public void reloadByFileName(String fileName) throws JoranException,
      FileNotFoundException {
    File f = new File(fileName);
    if (f.exists() && f.isFile()) {
      URL url;
      try {
        url = f.toURI().toURL();
        reloadByURL(url);
      } catch (MalformedURLException e) {
        throw new RuntimeException(
            "Unexpected MalformedURLException occured. See nexted cause.", e);
      }

    } else {
      String errMsg = "Could not find [" + fileName + "]";
      addInfo(errMsg);
      throw new FileNotFoundException(errMsg);
    }
// END reloadByFileName(String-String)//  }
	

    // START addStatusListener(StatusListener-StatusListener)//void addStatusListener(StatusListener statusListener) {
    StatusManager sm = loggerContext.getStatusManager();
    sm.add(statusListener);
// END addStatusListener(StatusListener-StatusListener)//  }
	

    // START removeStatusListener(StatusListener-StatusListener)//void removeStatusListener(StatusListener statusListener) {
    StatusManager sm = loggerContext.getStatusManager();
    sm.remove(statusListener);
// END removeStatusListener(StatusListener-StatusListener)//  }
	

    public void reloadByURL(URL url) throws JoranException {
        StatusListenerAsList statusListenerAsList = new StatusListenerAsList();

        addStatusListener(statusListenerAsList);
        addInfo("Resetting context: " + loggerContext.getName());
        loggerContext.reset();

        // after a reset the statusListenerAsList gets removed as a listener
        addStatusListener(statusListenerAsList);

        try {
            if (url != null) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(loggerContext);
                configurator.doConfigure(url);
                addInfo("Context: " + loggerContext.getName() + " reloaded.");
            }
        } finally {
            removeStatusListener(statusListenerAsList);
            if (debug) {
                StatusPrinter.print(statusListenerAsList.getStatusList());
            }
        }
    }
	

    // START setLoggerLevel(String-String-String-String)//public void setLoggerLevel(String loggerName, String levelStr) {
    if (loggerName == null) {
      return;
    }
    if (levelStr == null) {
      return;
    }
    loggerName = loggerName.trim();
    levelStr = levelStr.trim();

    addInfo("Trying to set level " + levelStr + " to logger " + loggerName);
    LoggerContext lc = (LoggerContext) context;

    Logger logger = lc.getLogger(loggerName);
    if ("null".equalsIgnoreCase(levelStr)) {
      logger.setLevel(null);
    } else {
      Level level = Level.toLevel(levelStr, null);
      if (level != null) {
        logger.setLevel(level);
      }
    }
// END setLoggerLevel(String-String-String-String)//  }
	

    // START getLoggerLevel(String-String)//public String getLoggerLevel(String loggerName) {
    if (loggerName == null) {
      return EMPTY;
    }

    loggerName = loggerName.trim();

    LoggerContext lc = (LoggerContext) context;
    Logger logger = lc.exists(loggerName);
    if (logger != null && logger.getLevel() != null) {
      return logger.getLevel().toString();
    } else {
      return EMPTY;
    }
// END getLoggerLevel(String-String)//  }
	

    // START getLoggerEffectiveLevel(String-String)//public String getLoggerEffectiveLevel(String loggerName) {
    if (loggerName == null) {
      return EMPTY;
    }

    loggerName = loggerName.trim();

    LoggerContext lc = (LoggerContext) context;
    Logger logger = lc.exists(loggerName);
    if (logger != null) {
      return logger.getEffectiveLevel().toString();
    } else {
      return EMPTY;
    }
// END getLoggerEffectiveLevel(String-String)//  }
	

    // START getLoggerList({FormalParametersInternal})//public List<String> getLoggerList() {
    LoggerContext lc = (LoggerContext) context;
    List<String> strList = new ArrayList<String>();
    Iterator<Logger> it = lc.getLoggerList().iterator();
    while (it.hasNext()) {
      Logger log = it.next();
      strList.add(log.getName());
    }
    return strList;
// END getLoggerList({FormalParametersInternal})//  }
	

    // START getStatuses({FormalParametersInternal})//public List<String> getStatuses() {
    List<String> list = new ArrayList<String>();
    Iterator<Status> it = context.getStatusManager().getCopyOfStatusList()
        .iterator();
    while (it.hasNext()) {
      list.add(it.next().toString());
    }
    return list;
// END getStatuses({FormalParametersInternal})//  }
	

    /**
     * When the associated LoggerContext is stopped, this configurator must be
     * unregistered
     */
    // START onStop(LoggerContext-LoggerContext)//public void onStop(LoggerContext context) {
    if (!started) {
      addInfo("onStop() method called on a stopped JMXActivator ["
          + objectNameAsString + "]");
      return;
    }
    if (mbs.isRegistered(objectName)) {
      try {
        addInfo("Unregistering mbean [" + objectNameAsString + "]");
        mbs.unregisterMBean(objectName);
      } catch (InstanceNotFoundException e) {
        // this is theoretically impossible
        addError("Unable to find a verifiably registered mbean ["
            + objectNameAsString + "]", e);
      } catch (MBeanRegistrationException e) {
        addError("Failed to unregister [" + objectNameAsString + "]", e);
      }
    } else {
      addInfo("mbean [" + objectNameAsString
          + "] was not in the mbean registry. This is OK.");
    }
    stop();
// END onStop(LoggerContext-LoggerContext)//  }
	

    // START onLevelChange(Logger-Logger-Level-Level)//public void onLevelChange(Logger logger, Level level) {
    // nothing to do
// END onLevelChange(Logger-Logger-Level-Level)//  }
	

    // START onReset(LoggerContext-LoggerContext)//public void onReset(LoggerContext context) {
    addInfo("onReset() method called JMXActivator [" + objectNameAsString + "]");
// END onReset(LoggerContext-LoggerContext)//  }
	

    /**
     * JMXConfigurator should not be removed subsequent to a LoggerContext reset.
     * 
     * @return
     */
    // START isResetResistant({FormalParametersInternal})//public boolean isResetResistant() {
    return true;
// END isResetResistant({FormalParametersInternal})//  }
	

    // START clearFields({FormalParametersInternal})//private void clearFields() {
    mbs = null;
    objectName = null;
    loggerContext = null;
// END clearFields({FormalParametersInternal})//  }
	

    // START stop({FormalParametersInternal})//private void stop() {
    started = false;
    clearFields();
// END stop({FormalParametersInternal})//  }
	

    // START onStart(LoggerContext-LoggerContext)//public void onStart(LoggerContext context) {
    // nop
// END onStart(LoggerContext-LoggerContext)//  }
	

    // START toString({FormalParametersInternal})//@Override
  public String toString() {
    return this.getClass().getName() + "(" + context.getName() + ")";
// END toString({FormalParametersInternal})//  }

}
