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

import java.io.BufferedReader;
 
import java.io.PrintWriter;
 
import java.io.StringReader;
 
import java.io.StringWriter;
 
import java.lang.reflect.InvocationTargetException;
 
import java.util.Arrays;
 
import java.util.List;
 

import ch.qos.logback.core.CoreConstants;
 
import org.junit.After;
 
import org.junit.Before;
 
import org.junit.Test;
 

import ch.qos.logback.classic.Level;
 
import ch.qos.logback.classic.Logger;
 
import ch.qos.logback.classic.LoggerContext;
 
import ch.qos.logback.classic.spi.ILoggingEvent;
 
import ch.qos.logback.classic.spi.LoggingEvent;
 
 

 
import static org.fest.assertions.Assertions.assertThat;
 
import static org.junit.Assert.*;
 
import static org.junit.Assume.assumeTrue;
 
import ch.qos.logback.classic.util.TestHelper; 

import static ch.qos.logback.classic.util.TestHelper.addSuppressed; 

public
  class
  ThrowableProxyConverterTest {
	

    LoggerContext lc = new LoggerContext();

	
    ThrowableProxyConverter tpc = new ThrowableProxyConverter();

	
    StringWriter sw = new StringWriter();

	
    PrintWriter pw = new PrintWriter(sw);

	

    // START setUp({FormalParametersInternal})//@Before
  public void setUp() throws Exception {
    tpc.setContext(lc);
    tpc.start();
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
    public void suppressed() throws InvocationTargetException, IllegalAccessException {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
                                                      // sense.
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            Exception fooException = new Exception("Foo");
            Exception barException = new Exception("Bar");
            addSuppressed(e, fooException);
            addSuppressed(e, barException);
            ex = e;
        }
        verify(ex);
    }
	

    @Test
    public void suppressedWithCause() throws InvocationTargetException, IllegalAccessException {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
                                                      // sense.
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            ex = new Exception("Wrapper", e);
            Exception fooException = new Exception("Foo");
            Exception barException = new Exception("Bar");
            addSuppressed(ex, fooException);
            addSuppressed(e, barException);
        }
        verify(ex);
    }
	

    @Test
    public void suppressedWithSuppressed() throws Exception {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
                                                      // sense.
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            ex = new Exception("Wrapper", e);
            Exception fooException = new Exception("Foo");
            Exception barException = new Exception("Bar");
            addSuppressed(barException, fooException);
            addSuppressed(e, barException);
        }
        verify(ex);
    }
	

    // START smoke({FormalParametersInternal})//@Test
  public void smoke() {
    Exception t = new Exception("smoke");
    verify(t);
// END smoke({FormalParametersInternal})//  }
	

    @Test
    public void nested() {
        Throwable t = TestHelper.makeNestedException(1);
        verify(t);
    }
	

    @Test
    public void withArgumentOfOne() throws Exception {
        final Throwable t = TestHelper.makeNestedException(0);
        t.printStackTrace(pw);
        final ILoggingEvent le = createLoggingEvent(t);

        final List<String> optionList = Arrays.asList("1");
        tpc.setOptionList(optionList);
        tpc.start();

        final String result = tpc.convert(le);

        final BufferedReader reader = new BufferedReader(new StringReader(result));
        assertTrue(reader.readLine().contains(t.getMessage()));
        assertNotNull(reader.readLine());
        assertNull("Unexpected line in stack trace", reader.readLine());
    }
	

    @Test
    public void withShortArgument() throws Exception {
        final Throwable t = TestHelper.makeNestedException(0);
        t.printStackTrace(pw);
        final ILoggingEvent le = createLoggingEvent(t);

        final List<String> options = Arrays.asList("short");
        tpc.setOptionList(options);
        tpc.start();

        final String result = tpc.convert(le);

        final BufferedReader reader = new BufferedReader(new StringReader(result));
        assertTrue(reader.readLine().contains(t.getMessage()));
        assertNotNull(reader.readLine());
        assertNull("Unexpected line in stack trace", reader.readLine());
    }
	

    @Test
    public void skipSelectedLine() throws Exception {
        // given
        final Throwable t = TestHelper.makeNestedException(0);
        t.printStackTrace(pw);
        final ILoggingEvent le = createLoggingEvent(t);
        tpc.setOptionList(Arrays.asList("full", "skipSelectedLines"));
        tpc.start();

        // when
        final String result = tpc.convert(le);

        // then
        assertThat(result).excludes("skipSelectedLines");
    }
	

    @Test
    public void skipMultipleLines() throws Exception {
        // given
        final Throwable t = TestHelper.makeNestedException(0);
        t.printStackTrace(pw);
        final ILoggingEvent le = createLoggingEvent(t);
        tpc.setOptionList(Arrays.asList("full", "skipMultipleLines", "junit"));
        tpc.start();

        // when
        final String result = tpc.convert(le);

        // then
        assertThat(result).excludes("skipSelectedLines").excludes("junit");
    }
	

    @Test
    public void shouldLimitTotalLinesExcludingSkipped() throws Exception {
        // given
        final Throwable t = TestHelper.makeNestedException(0);
        t.printStackTrace(pw);
        final ILoggingEvent le = createLoggingEvent(t);
        tpc.setOptionList(Arrays.asList("3", "shouldLimitTotalLinesExcludingSkipped"));
        tpc.start();

        // when
        final String result = tpc.convert(le);

        // then
        String[] lines = result.split(CoreConstants.LINE_SEPARATOR);
        assertThat(lines).hasSize(3 + 1);
    }
	

    // START someMethod({FormalParametersInternal})//void someMethod() throws Exception {
    throw new Exception("someMethod");
// END someMethod({FormalParametersInternal})//  }
	

    // START verify(Throwable-Throwable)//void verify(Throwable t) {
    t.printStackTrace(pw);

    ILoggingEvent le = createLoggingEvent(t);
    String result = tpc.convert(le);
    System.out.println(result);
    result = result.replace("common frames omitted", "more");
    assertEquals(sw.toString(), result);
// END verify(Throwable-Throwable)//  }

}
