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
package ch.qos.logback.classic.pattern; 

 
import static org.fest.assertions.Assertions.assertThat;
 

import java.io.PrintWriter;
 
import java.io.StringWriter;
 
import java.util.regex.Matcher;
 
import java.util.regex.Pattern;
 

import org.junit.After;
 
import org.junit.Before;
 
import org.junit.Test;
 

import ch.qos.logback.classic.Level;
 
import ch.qos.logback.classic.Logger;
 
import ch.qos.logback.classic.LoggerContext;
 
import ch.qos.logback.classic.PatternLayout;
 
import ch.qos.logback.classic.spi.ILoggingEvent;
 
import ch.qos.logback.classic.spi.LoggingEvent;
 

import static org.junit.Assert.*; 

public
  class
  ExtendedThrowableProxyConverterTest {
	

    LoggerContext lc = new LoggerContext();

	
    ExtendedThrowableProxyConverter etpc = new ExtendedThrowableProxyConverter();

	
    StringWriter sw = new StringWriter();

	
    PrintWriter pw = new PrintWriter(sw);

	

    // START setUp({FormalParametersInternal})//@Before
  public void setUp() throws Exception {
    lc.setPackagingDataEnabled(true);
    etpc.setContext(lc);
    etpc.start();
// END setUp({FormalParametersInternal})//  }
	

    // START tearDown({FormalParametersInternal})//@After
  public void tearDown() throws Exception {
// END tearDown({FormalParametersInternal})//  }
	

    // START createLoggingEvent(Throwable-Throwable)//private ILoggingEvent createLoggingEvent(Throwable t) {
    return new LoggingEvent(this.getClass().getName(), lc
        .getLogger(Logger.ROOT_LOGGER_NAME), Level.DEBUG, "test message", t,
        null);
// END createLoggingEvent(Throwable-Throwable)//  }
	

    @Test
    public void integration() {
        PatternLayout pl = new PatternLayout();
        pl.setContext(lc);
        pl.setPattern("%m%n%xEx");
        pl.start();
        ILoggingEvent e = createLoggingEvent(new Exception("x"));
        String res = pl.doLayout(e);

        // make sure that at least some package data was output
        Pattern p = Pattern.compile("\\s*at .*?\\[.*?\\]");
        Matcher m = p.matcher(res);
        int i = 0;
        while (m.find()) {
            i++;
        }
        assertThat(i).isGreaterThan(5);
    }
	

    // START smoke({FormalParametersInternal})//@Test
  public void smoke() {
    Exception t = new Exception("smoke");
    verify(t);
// END smoke({FormalParametersInternal})//  }
	

    // START nested({FormalParametersInternal})//@Test
  public void nested() {
    Throwable t = makeNestedException(1);
    verify(t);
// END nested({FormalParametersInternal})//  }
	

    // START verify(Throwable-Throwable)//void verify(Throwable t) {
    t.printStackTrace(pw);

    ILoggingEvent le = createLoggingEvent(t);
    String result = etpc.convert(le);
    result = result.replace("common frames omitted", "more");
    result = result.replaceAll(" ~?\\[.*\\]", "");
    assertEquals(sw.toString(), result);
// END verify(Throwable-Throwable)//  }
	

    // START makeNestedException(int-int)//Throwable makeNestedException(int level) {
    if (level == 0) {
      return new Exception("nesting level=" + level);
    }
    Throwable cause = makeNestedException(level - 1);
    return new Exception("nesting level =" + level, cause);
// END makeNestedException(int-int)//  }

}
