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
package ch.qos.logback.classic.spi; 

 
import static org.junit.Assert.assertEquals;
 
import static org.junit.Assume.assumeNotNull;
 
import static org.junit.Assume.assumeTrue;
 

import java.io.PrintWriter;
 
import java.io.StringWriter;
 
import java.lang.reflect.InvocationTargetException;
 
import java.lang.reflect.Method;
 

 
import org.junit.After;
 
import org.junit.Before;
 
import org.junit.Test;
 

import static ch.qos.logback.classic.util.TestHelper.addSuppressed; 

import ch.qos.logback.classic.util.TestHelper; 

public
  class
  ThrowableProxyTest {
	

    StringWriter sw = new StringWriter();

	
    PrintWriter pw = new PrintWriter(sw);

	

    // START setUp({FormalParametersInternal})//@Before
  public void setUp() throws Exception {
// END setUp({FormalParametersInternal})//  }
	

    // START tearDown({FormalParametersInternal})//@After
  public void tearDown() throws Exception {
// END tearDown({FormalParametersInternal})//  }
	

    // START verify(Throwable-Throwable)//public void verify(Throwable t) {
    t.printStackTrace(pw);

    IThrowableProxy tp = new ThrowableProxy(t);

    String result = ThrowableProxyUtil.asString(tp);
    result = result.replace("common frames omitted", "more");

    String expected = sw.toString();

    System.out.println("========expected");
    System.out.println(expected);

    System.out.println("========result");
    System.out.println(result);

    assertEquals(expected, result);
// END verify(Throwable-Throwable)//  }
	

    // START smoke({FormalParametersInternal})//@Test
  public void smoke() {
    Exception e = new Exception("smoke");
    verify(e);
// END smoke({FormalParametersInternal})//  }
	

    // START nested({FormalParametersInternal})//@Test
  public void nested() {
    Exception w = null;
    try {
      someMethod();
    } catch (Exception e) {
      w = new Exception("wrapping", e);
    }
    verify(w);
// END nested({FormalParametersInternal})//  }
	

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
	

    // see also http://jira.qos.ch/browse/LBCLASSIC-216
    // START nullSTE({FormalParametersInternal})//@Test
  public void nullSTE() {
    Throwable t = new Exception("someMethodWithNullException") {
      @Override
      public StackTraceElement[] getStackTrace() {
        return null;
      }
    };
    // we can't test output as Throwable.printStackTrace method uses
    // the private getOurStackTrace method instead of getStackTrace

    // tests  ThrowableProxyUtil.steArrayToStepArray
    new ThrowableProxy(t);

    // tests  ThrowableProxyUtil.findNumberOfCommonFrames
    Exception top = new Exception("top", t);
    new ThrowableProxy(top);
// END nullSTE({FormalParametersInternal})//  }
	

    // START multiNested({FormalParametersInternal})//@Test
  public void multiNested() {
    Exception w = null;
    try {
      someOtherMethod();
    } catch (Exception e) {
      w = new Exception("wrapping", e);
    }
    verify(w);
// END multiNested({FormalParametersInternal})//  }
	

    // START someMethod({FormalParametersInternal})//void someMethod() throws Exception {
    throw new Exception("someMethod");
// END someMethod({FormalParametersInternal})//  }
	

    // START someMethodWithNullException({FormalParametersInternal})//void someMethodWithNullException() throws Exception {
    throw new Exception("someMethodWithNullException") {
      @Override
      public StackTraceElement[] getStackTrace() {
        return null;
      }
    };
// END someMethodWithNullException({FormalParametersInternal})//  }
	

    // START someOtherMethod({FormalParametersInternal})//void someOtherMethod() throws Exception {
    try {
      someMethod();
    } catch (Exception e) {
      throw new Exception("someOtherMethod", e);
    }
// END someOtherMethod({FormalParametersInternal})//  }

}
