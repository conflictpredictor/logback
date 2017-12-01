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
package ch.qos.logback.core.joran; 

import ch.qos.logback.core.Context;
 
import ch.qos.logback.core.joran.event.SaxEvent;
 
import ch.qos.logback.core.joran.event.SaxEventRecorder;
 
import ch.qos.logback.core.joran.spi.*;
 
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
 
import ch.qos.logback.core.spi.ContextAwareBase;
 
import ch.qos.logback.core.status.StatusUtil;
 

import org.xml.sax.InputSource;
 

import java.io.File;
 
import java.io.FileInputStream;
 
import java.io.IOException;
 
import java.io.InputStream;
 
import java.net.URL;
 
import java.net.URLConnection;
 
import java.util.List;
 

import static ch.qos.logback.core.CoreConstants.SAFE_JORAN_CONFIGURATION;
 

public abstract
  class
  GenericConfigurator  extends ContextAwareBase
 {
	

    protected Interpreter interpreter;

	

    // START doConfigure(URL-URL)//public final void doConfigure(URL url) throws JoranException {
    InputStream in = null;
    try {
      informContextOfURLUsedForConfiguration(getContext(), url);
      URLConnection urlConnection = url.openConnection();
      // per http://jira.qos.ch/browse/LBCORE-105
      // per http://jira.qos.ch/browse/LBCORE-127
      urlConnection.setUseCaches(false);

      in = urlConnection.getInputStream();
      doConfigure(in);
    } catch (IOException ioe) {
      String errMsg = "Could not open URL [" + url + "].";
      addError(errMsg, ioe);
      throw new JoranException(errMsg, ioe);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException ioe) {
          String errMsg = "Could not close input stream";
          addError(errMsg, ioe);
          throw new JoranException(errMsg, ioe);
        }
      }
    }
// END doConfigure(URL-URL)//  }
	

    // START doConfigure(String-String)//public final void doConfigure(String filename) throws JoranException {
    doConfigure(new File(filename));
// END doConfigure(String-String)//  }
	

    // START doConfigure(File-File)//public final void doConfigure(File file) throws JoranException {
    FileInputStream fis = null;
    try {
      informContextOfURLUsedForConfiguration(getContext(), file.toURI().toURL());
      fis = new FileInputStream(file);
      doConfigure(fis);
    } catch (IOException ioe) {
      String errMsg = "Could not open [" + file.getPath() + "].";
      addError(errMsg, ioe);
      throw new JoranException(errMsg, ioe);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (java.io.IOException ioe) {
          String errMsg = "Could not close [" + file.getName() + "].";
          addError(errMsg, ioe);
          throw new JoranException(errMsg, ioe);
        }
      }
    }
// END doConfigure(File-File)//  }
	

    // START informContextOfURLUsedForConfiguration(Context-Context-URL-URL)//public static void informContextOfURLUsedForConfiguration(Context context, URL url) {
    ConfigurationWatchListUtil.setMainWatchURL(context, url);
// END informContextOfURLUsedForConfiguration(Context-Context-URL-URL)//  }
	

    // START doConfigure(InputStream-InputStream)//public final void doConfigure(InputStream inputStream) throws JoranException {
    doConfigure(new InputSource(inputStream));
// END doConfigure(InputStream-InputStream)//  }
	

    protected abstract void addInstanceRules(RuleStore rs);

	

    protected abstract void addImplicitRules(Interpreter interpreter);

	

    // START addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry-DefaultNestedComponentRegistry)//protected void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {

// END addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry-DefaultNestedComponentRegistry)//  }
	

    // START initialElementPath({FormalParametersInternal})//protected ElementPath initialElementPath() {
    return new ElementPath();
// END initialElementPath({FormalParametersInternal})//  }
	

    // START buildInterpreter({FormalParametersInternal})//protected void buildInterpreter() {
    RuleStore rs = new SimpleRuleStore(context);
    addInstanceRules(rs);
    this.interpreter = new Interpreter(context, rs, initialElementPath());
    InterpretationContext interpretationContext = interpreter.getInterpretationContext();
    interpretationContext.setContext(context);
    addImplicitRules(interpreter);
    addDefaultNestedComponentRegistryRules(interpretationContext.getDefaultNestedComponentRegistry());
// END buildInterpreter({FormalParametersInternal})//  }
	

    // this is the most inner form of doConfigure whereto other doConfigure
    // methods ultimately delegate
    // START doConfigure(InputSource-InputSource)//public final void doConfigure(final InputSource inputSource)
          throws JoranException {

    long threshold = System.currentTimeMillis();
    if (!ConfigurationWatchListUtil.wasConfigurationWatchListReset(context)) {
      informContextOfURLUsedForConfiguration(getContext(), null);
    }
    SaxEventRecorder recorder = new SaxEventRecorder(context);
    recorder.recordEvents(inputSource);
    doConfigure(recorder.saxEventList);
    // no exceptions a this level
    StatusUtil statusUtil = new StatusUtil(context);
    if (statusUtil.noXMLParsingErrorsOccurred(threshold)) {
      addInfo("Registering current configuration as safe fallback point");
      registerSafeConfiguration();
    }
// END doConfigure(InputSource-InputSource)//  }
	

    // START doConfigure(List<SaxEvent>-List<SaxEvent>)//public void doConfigure(final List<SaxEvent> eventList)
          throws JoranException {
    buildInterpreter();
    // disallow simultaneous configurations of the same context
    synchronized (context.getConfigurationLock()) {
      interpreter.getEventPlayer().play(eventList);
    }
// END doConfigure(List<SaxEvent>-List<SaxEvent>)//  }
	

    /**
     * Register the current event list in currently in the interpreter as a safe
     * configuration point.
     *
     * @since 0.9.30
     */
    // START registerSafeConfiguration({FormalParametersInternal})//public void registerSafeConfiguration() {
    context.putObject(SAFE_JORAN_CONFIGURATION, interpreter.getEventPlayer().getCopyOfPlayerEventList());
// END registerSafeConfiguration({FormalParametersInternal})//  }
	

    /**
     * Recall the event list previously registered as a safe point.
     */
    @SuppressWarnings("unchecked")
    public List<SaxEvent> recallSafeConfiguration() {
        return (List<SaxEvent>) context.getObject(SAFE_JORAN_CONFIGURATION);
    }

}
