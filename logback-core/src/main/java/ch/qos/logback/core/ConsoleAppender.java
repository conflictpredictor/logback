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

 

import java.io.OutputStream;
 
import java.util.Arrays;
 

import ch.qos.logback.core.joran.spi.ConsoleTarget;
 
import ch.qos.logback.core.status.Status;
 
import ch.qos.logback.core.status.WarnStatus;
 
 
import ch.qos.logback.core.util.EnvUtil;
 
 
import ch.qos.logback.core.util.OptionHelper;
 
 

/**
 * ConsoleAppender appends log events to <code>System.out</code> or
 * <code>System.err</code> using a layout specified by the user. The default
 * target is <code>System.out</code>.
 * <p/>
 * For more information about this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#ConsoleAppender
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Tom SH Liu
 * @author Ruediger Dohna
 */

public
  class
  ConsoleAppender <E>
  extends OutputStreamAppender<E>
 {
	

    protected ConsoleTarget target = ConsoleTarget.SystemOut;

	
    protected boolean withJansi = false;

	

    private final static String WindowsAnsiOutputStream_CLASS_NAME = "org.fusesource.jansi.WindowsAnsiOutputStream";

	

    /**
     * Sets the value of the <b>Target</b> option. Recognized values are
     * "System.out" and "System.err". Any other value will be ignored.
     */
    // START setTarget(String-String)//public void setTarget(String value) {
    ConsoleTarget t = ConsoleTarget.findByName(value.trim());
    if (t == null) {
      targetWarn(value);
    } else {
      target = t;
    }
// END setTarget(String-String)//  }
	

    /**
     * Returns the current value of the <b>target</b> property. The default value
     * of the option is "System.out".
     * <p/>
     * See also {@link #setTarget}.
     */
    // START getTarget({FormalParametersInternal})//public String getTarget() {
    return target.getName();
// END getTarget({FormalParametersInternal})//  }
	

    // START targetWarn(String-String)//private void targetWarn(String val) {
    Status status = new WarnStatus("[" + val + "] should be one of "
            + Arrays.toString(ConsoleTarget.values()), this);
    status.add(new WarnStatus(
            "Using previously set target, System.out by default.", this));
    addStatus(status);
// END targetWarn(String-String)//  }
	

    // START start({FormalParametersInternal})//@Override
  public void start() {
    OutputStream targetStream = target.getStream();
    // enable jansi only on Windows and only if withJansi set to true
    if (EnvUtil.isWindows() && withJansi) {
      targetStream = getTargetStreamForWindows(targetStream);
    }
    setOutputStream(targetStream);
    super.start();
// END start({FormalParametersInternal})//  }
	

    // START getTargetStreamForWindows(OutputStream-OutputStream)//private OutputStream getTargetStreamForWindows(OutputStream targetStream) {
    try {
      addInfo("Enabling JANSI WindowsAnsiOutputStream for the console.");
      Object windowsAnsiOutputStream = OptionHelper.instantiateByClassNameAndParameter(WindowsAnsiOutputStream_CLASS_NAME, Object.class, context,
              OutputStream.class, targetStream);
      return (OutputStream) windowsAnsiOutputStream;
    } catch (Exception e) {
      addWarn("Failed to create WindowsAnsiOutputStream. Falling back on the default stream.", e);
    }
    return targetStream;
// END getTargetStreamForWindows(OutputStream-OutputStream)//  }
	

    /**
     * @return
     */
    // START isWithJansi({FormalParametersInternal})//public boolean isWithJansi() {
    return withJansi;
// END isWithJansi({FormalParametersInternal})//  }
	

    /**
     * If true, this appender will output to a stream which
     *
     * @param withJansi
     * @since 1.0.5
     */
    // START setWithJansi(boolean-boolean)//public void setWithJansi(boolean withJansi) {
    this.withJansi = withJansi;
// END setWithJansi(boolean-boolean)//  }

}
