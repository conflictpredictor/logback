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
package ch.qos.logback.core.joran.action; 

import org.xml.sax.Attributes;
 
import org.xml.sax.Locator;
 

import ch.qos.logback.core.joran.spi.ActionException;
 
import ch.qos.logback.core.joran.spi.InterpretationContext;
 
import ch.qos.logback.core.joran.spi.Interpreter;
 
import ch.qos.logback.core.spi.ContextAwareBase;
 

/**
 *
 * Most of the work for configuring logback is done by Actions.
 *
 * <p>Action methods are invoked as the XML file is parsed.
 *
 * <p>This class is largely inspired from the relevant class in the
 * commons-digester project of the Apache Software Foundation.
 *
 * @author Craig McClanahan
 * @author Christopher Lenz
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public abstract
  class
  Action  extends ContextAwareBase
 {
	

    public static final String NAME_ATTRIBUTE = "name";

	
    public static final String KEY_ATTRIBUTE = "key";

	
    public static final String VALUE_ATTRIBUTE = "value";

	
    public static final String FILE_ATTRIBUTE = "file";

	
    public static final String CLASS_ATTRIBUTE = "class";

	
    public static final String PATTERN_ATTRIBUTE = "pattern";

	
    public static final String SCOPE_ATTRIBUTE = "scope";

	

    public static final String ACTION_CLASS_ATTRIBUTE = "actionClass";

	

    /**
     * Called when the parser encounters an element matching a
     * {@link ch.qos.logback.core.joran.spi.ElementSelector Pattern}.
     */
    // START begin(InterpretationContext-InterpretationContext-String-String-Attributes-Attributes)//public abstract void begin(InterpretationContext ic, String name,
// END begin(InterpretationContext-InterpretationContext-String-String-Attributes-Attributes)//      Attributes attributes) throws ActionException;
	

    /**
     * Called to pass the body (as text) contained within an element.
     * @param ic
     * @param body
     * @throws ActionException
     */
    // START body(InterpretationContext-InterpretationContext-String-String)//public void body(InterpretationContext ic, String body)
      throws ActionException {
    // NOP
// END body(InterpretationContext-InterpretationContext-String-String)//  }
	

    /*
     * Called when the parser encounters an endElement event matching a {@link ch.qos.logback.core.joran.spi.Pattern
     * Pattern}.
     */
    // START end(InterpretationContext-InterpretationContext-String-String)//public abstract void end(InterpretationContext ic, String name)
// END end(InterpretationContext-InterpretationContext-String-String)//      throws ActionException;
	

    // START toString({FormalParametersInternal})//public String toString() {
    return this.getClass().getName();
// END toString({FormalParametersInternal})//  }
	

    // START getColumnNumber(InterpretationContext-InterpretationContext)//protected int getColumnNumber(InterpretationContext ic) {
    Interpreter ji = ic.getJoranInterpreter();
    Locator locator = ji.getLocator();
    if (locator != null) {
      return locator.getColumnNumber();
    }
    return -1;
// END getColumnNumber(InterpretationContext-InterpretationContext)//  }
	

    // START getLineNumber(InterpretationContext-InterpretationContext)//protected int getLineNumber(InterpretationContext ic) {
    Interpreter ji = ic.getJoranInterpreter();
    Locator locator = ji.getLocator();
    if (locator != null) {
      return locator.getLineNumber();
    }
    return -1;
// END getLineNumber(InterpretationContext-InterpretationContext)//  }
	

    // START getLineColStr(InterpretationContext-InterpretationContext)//protected String getLineColStr(InterpretationContext ic) {
    return "line: " + getLineNumber(ic) + ", column: "
        + getColumnNumber(ic);
// END getLineColStr(InterpretationContext-InterpretationContext)//  }

}
