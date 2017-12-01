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

import java.io.File;
 
import java.io.IOException;
 
import java.nio.channels.FileChannel;
 
import java.nio.channels.FileLock;
 

import ch.qos.logback.core.recovery.ResilientFileOutputStream;
 
import ch.qos.logback.core.util.FileUtil;
 

import static ch.qos.logback.core.CoreConstants.CODES_URL; 
import static ch.qos.logback.core.CoreConstants.MORE_INFO_PREFIX; 
import java.util.Map; 
import java.util.Map.Entry; 

/**
 * FileAppender appends log events to a file.
 * 
 * For more information about this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#FileAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public
  class
  FileAppender <E>
  extends OutputStreamAppender<E>
 {
	

    /**
     * Append to or truncate the file? The default value for this variable is
     * <code>true</code>, meaning that by default a <code>FileAppender</code> will
     * append to an existing file and not truncate it.
     */
    protected boolean append = true;

	

    /**
     * The name of the active log file.
     */
    protected String fileName = null;

	

    private boolean prudent = false;

	

    /**
     * The <b>File</b> property takes a string value which should be the name of
     * the file to append to.
     */
    // START setFile(String-String)//public void setFile(String file) {
    if (file == null) {
      fileName = file;
    } else {
      // Trim spaces from both ends. The users probably does not want
      // trailing spaces in file names.
      fileName = file.trim();
    }
// END setFile(String-String)//  }
	

    /**
     * Returns the value of the <b>Append</b> property.
     */
    // START isAppend({FormalParametersInternal})//public boolean isAppend() {
    return append;
// END isAppend({FormalParametersInternal})//  }
	

    /**
     * This method is used by derived classes to obtain the raw file property.
     * Regular users should not be calling this method.
     * 
     * @return the value of the file property
     */
    // START rawFileProperty({FormalParametersInternal})//final public String rawFileProperty() {
    return fileName;
// END rawFileProperty({FormalParametersInternal})//  }
	

    /**
     * Returns the value of the <b>File</b> property.
     * 
     * <p>
     * This method may be overridden by derived classes.
     * 
     */
    // START getFile({FormalParametersInternal})//public String getFile() {
    return fileName;
// END getFile({FormalParametersInternal})//  }
	

    /**
     * If the value of <b>File</b> is not <code>null</code>, then
     * {@link #openFile} is called with the values of <b>File</b> and
     * <b>Append</b> properties.
     */
    public void start() {
        int errors = 0;
        if (getFile() != null) {
            addInfo("File property is set to [" + fileName + "]");

            if (checkForFileCollisionInPreviousFileAppenders()) {
                addError("Collisions detected with FileAppender/RollingAppender instances defined earlier. Aborting.");
                addError(MORE_INFO_PREFIX + COLLISION_WITH_EARLIER_APPENDER_URL);
                errors++;
            }

            if (prudent) {
                if (!isAppend()) {
                    setAppend(true);
                    addWarn("Setting \"Append\" property to true on account of \"Prudent\" mode");
                }
            }

            try {
                openFile(getFile());
            } catch (java.io.IOException e) {
                errors++;
                addError("openFile(" + fileName + "," + append + ") call failed.", e);
            }
        } else {
            errors++;
            addError("\"File\" property not set for appender named [" + name + "].");
        }
        if (errors == 0) {
            super.start();
        }
    }
	

    /**
     * <p>
     * Sets and <i>opens</i> the file where the log output will go. The specified
     * file must be writable.
     * 
     * <p>
     * If there was already an opened file, then the previous file is closed
     * first.
     * 
     * <p>
     * <b>Do not use this method directly. To configure a FileAppender or one of
     * its subclasses, set its properties one by one and then call start().</b>
     * 
     * @param file_name
     *          The path to the log file.
     */
    // START openFile(String-String)//public void openFile(String file_name) throws IOException {
    lock.lock();
    try {
      File file = new File(file_name);
      boolean result = FileUtil.createMissingParentDirectories(file);
      if (!result) {
        addError("Failed to create parent directories for ["
            + file.getAbsolutePath() + "]");
      }

      ResilientFileOutputStream resilientFos = new ResilientFileOutputStream(
          file, append);
      resilientFos.setContext(context);
      setOutputStream(resilientFos);
    } finally {
      lock.unlock();
    }
// END openFile(String-String)//  }
	

    /**
     * @see #setPrudent(boolean)
     * 
     * @return true if in prudent mode
     */
    // START isPrudent({FormalParametersInternal})//public boolean isPrudent() {
    return prudent;
// END isPrudent({FormalParametersInternal})//  }
	

    /**
     * When prudent is set to true, file appenders from multiple JVMs can safely
     * write to the same file.
     * 
     * @param prudent
     */
    // START setPrudent(boolean-boolean)//public void setPrudent(boolean prudent) {
    this.prudent = prudent;
// END setPrudent(boolean-boolean)//  }
	

    // START setAppend(boolean-boolean)//public void setAppend(boolean append) {
    this.append = append;
// END setAppend(boolean-boolean)//  }
	

    // START safeWrite(E-E)//private void safeWrite(E event) throws IOException {
    ResilientFileOutputStream resilientFOS = (ResilientFileOutputStream) getOutputStream();
    FileChannel fileChannel = resilientFOS.getChannel();
    if (fileChannel == null) {
      return;
    }

    // Clear any current interrupt (see LOGBACK-875)
    boolean interrupted = Thread.interrupted();

    FileLock fileLock = null;
    try {
      fileLock = fileChannel.lock();
      long position = fileChannel.position();
      long size = fileChannel.size();
      if (size != position) {
        fileChannel.position(size);
      }
      super.writeOut(event);
    } catch (IOException e) {
      // Mainly to catch FileLockInterruptionExceptions (see LOGBACK-875)
      resilientFOS.postIOFailure(e);
    }
    finally {
      if (fileLock != null && fileLock.isValid()) {
        fileLock.release();
      }

      // Re-interrupt if we started in an interrupted state (see LOGBACK-875)
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
// END safeWrite(E-E)//  }
	

    // START writeOut(E-E)//@Override
  protected void writeOut(E event) throws IOException {
    if (prudent) {
      safeWrite(event);
    } else {
      super.writeOut(event);
    }
// END writeOut(E-E)//  }
	

    static protected String COLLISION_WITH_EARLIER_APPENDER_URL = CODES_URL + "#earlier_fa_collision";
	

    protected boolean checkForFileCollisionInPreviousFileAppenders() {
        boolean collisionsDetected = false;
        if (fileName == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) context.getObject(CoreConstants.RFA_FILENAME_PATTERN_COLLISION_MAP);
        if (map == null) {
            return collisionsDetected;
        }
        for (Entry<String, String> entry : map.entrySet()) {
            if (fileName.equals(entry.getValue())) {
                addErrorForCollision("File", entry.getValue(), entry.getKey());
                collisionsDetected = true;
            }
        }
        if (name != null) {
            map.put(getName(), fileName);
        }
        return collisionsDetected;
    }
	

    protected void addErrorForCollision(String optionName, String optionValue, String appenderName) {
        addError("'" + optionName + "' option has the same value \"" + optionValue + "\" as that given for appender [" + appenderName + "] defined earlier.");
    }

}
