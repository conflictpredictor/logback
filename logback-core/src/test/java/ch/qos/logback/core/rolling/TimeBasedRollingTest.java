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
package ch.qos.logback.core.rolling; 

import ch.qos.logback.core.encoder.EchoEncoder;
 
import ch.qos.logback.core.testUtil.EnvUtilForTests;
 
import ch.qos.logback.core.util.StatusPrinter;
 

import org.junit.After;
 
import org.junit.Before;
 
import org.junit.Test;
 

import java.io.File;
 
import java.io.FileOutputStream;
 
import java.io.IOException;
 
 

/**
 * A rather exhaustive set of tests. Tests include leaving the file option
 * blank, or setting it, with and without compression, and tests with or without
 * stopping/restarting the RollingFileAppender.
 * <p/>
 * The regression tests log a few times using a RollingFileAppender. Then, they
 * predict the names of the files which should be generated and compare them
 * with witness files.
 * <p/>
 * <pre>
 *                Compression     file option    Stop/Restart
 *     Test1      NO              BLANK           NO
 *     Test2      YES             BLANK           NO
 *     Test3      NO              BLANK           YES
 *     Test4      NO              SET             YES
 *     Test5      NO              SET             NO
 *     Test6      YES             SET             NO
 * </pre>
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public
  class
  TimeBasedRollingTest  extends ScaffoldingForRollingTests
 {
	

    static final int NO_RESTART = 0;

	
    static final int WITH_RESTART = 1;

	
    static final int WITH_RESTART_AND_LONG_WAIT = 2000;

	

    static final boolean FILE_OPTION_SET = true;

	
    static final boolean FILE_OPTION_BLANK = false;

	

    RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();

	
    TimeBasedRollingPolicy<Object> tbrp1 = new TimeBasedRollingPolicy<Object>();

	

    RollingFileAppender<Object> rfa2 = new RollingFileAppender<Object>();

	
    TimeBasedRollingPolicy<Object> tbrp2 = new TimeBasedRollingPolicy<Object>();

	

    EchoEncoder<Object> encoder = new EchoEncoder<Object>();

	

    RolloverChecker rolloverChecker;

	

    // START setUp({FormalParametersInternal})//@Before
  @Override
  public void setUp() {
    super.setUp();
// END setUp({FormalParametersInternal})//  }
	

    // START tearDown({FormalParametersInternal})//@After
  public void tearDown() {
// END tearDown({FormalParametersInternal})//  }
	

    // START initRFA(RollingFileAppender<Object>-RollingFileAppender<Object>-String-String)//void initRFA(RollingFileAppender<Object> rfa, String filename) {
    rfa.setContext(context);
    rfa.setEncoder(encoder);
    if (filename != null) {
      rfa.setFile(filename);
    }
// END initRFA(RollingFileAppender<Object>-RollingFileAppender<Object>-String-String)//  }
	

    // START initTRBP(RollingFileAppender<Object>-RollingFileAppender<Object>-TimeBasedRollingPolicy<Object>-TimeBasedRollingPolicy<Object>-String-String-long-long)//void initTRBP(RollingFileAppender<Object> rfa,
                TimeBasedRollingPolicy<Object> tbrp, String filenamePattern,
                long givenTime) {
    tbrp.setContext(context);
    tbrp.setFileNamePattern(filenamePattern);
    tbrp.setParent(rfa);
    tbrp.timeBasedFileNamingAndTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(givenTime);
    rfa.setRollingPolicy(tbrp);
    tbrp.start();
    rfa.start();
// END initTRBP(RollingFileAppender<Object>-RollingFileAppender<Object>-TimeBasedRollingPolicy<Object>-TimeBasedRollingPolicy<Object>-String-String-long-long)//  }
	

    // START genericTest(String-String-String-String-String-String-boolean-boolean-int-int)//void genericTest(String testId, String patternPrefix, String compressionSuffix, boolean fileOptionIsSet, int waitDuration) throws IOException {
    String fileName = fileOptionIsSet ? testId2FileName(testId) : null;
    initRFA(rfa1, fileName);

    String fileNamePatternStr = randomOutputDir + patternPrefix + "-%d{" + DATE_PATTERN_WITH_SECONDS + "}" + compressionSuffix;

    initTRBP(rfa1, tbrp1, fileNamePatternStr, currentTime);

    // compute the current filename
    addExpectedFileName_ByDate(fileNamePatternStr, getMillisOfCurrentPeriodsStart());

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(fileNamePatternStr);
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
      add(tbrp1.future);
    }
    rfa1.stop();
    waitForJobsToComplete();

    if (waitDuration != NO_RESTART) {
      doRestart(testId, patternPrefix, fileOptionIsSet, waitDuration);
    }
    waitForJobsToComplete();

    massageExpectedFilesToCorresponToCurrentTarget(fileName, fileOptionIsSet);
    StatusPrinter.print(context);
    rolloverChecker.check(expectedFilenameList);
// END genericTest(String-String-String-String-String-String-boolean-boolean-int-int)//  }
	

    // START defaultTest(String-String-String-String-String-String-boolean-boolean-int-int)//void defaultTest(String testId, String patternPrefix, String compressionSuffix, boolean fileOptionIsSet, int waitDuration) throws IOException {
    boolean withCompression = compressionSuffix.length() > 0;
    rolloverChecker = new DefaultRolloverChecker(testId, withCompression, compressionSuffix);
    genericTest(testId, patternPrefix, compressionSuffix, fileOptionIsSet, waitDuration);
// END defaultTest(String-String-String-String-String-String-boolean-boolean-int-int)//  }
	

    // START doRestart(String-String-String-String-boolean-boolean-int-int)//void doRestart(String testId, String patternPart, boolean fileOptionIsSet, int waitDuration) {
    // change the timestamp of the currently actively file
    File activeFile = new File(rfa1.getFile());
    activeFile.setLastModified(currentTime);

    incCurrentTime(waitDuration);

    String filePatternStr = randomOutputDir + patternPart + "-%d{" + DATE_PATTERN_WITH_SECONDS + "}";

    String fileName = fileOptionIsSet ? testId2FileName(testId) : null;
    initRFA(rfa2, fileName);
    initTRBP(rfa2, tbrp2, filePatternStr, currentTime);
    for (int i = 0; i < 3; i++) {
      rfa2.doAppend("World---" + i);
      addExpectedFileNamedIfItsTime_ByDate(filePatternStr);
      incCurrentTime(100);
      tbrp2.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
      add(tbrp2.future);
    }
    rfa2.stop();
// END doRestart(String-String-String-String-boolean-boolean-int-int)//  }
	

    // START noCompression_FileBlank_NoRestart_1({FormalParametersInternal})//@Test
  public void noCompression_FileBlank_NoRestart_1() throws IOException {
    defaultTest("test1", "test1", "", FILE_OPTION_BLANK, NO_RESTART);
// END noCompression_FileBlank_NoRestart_1({FormalParametersInternal})//  }
	

    // START withCompression_FileBlank_NoRestart_2({FormalParametersInternal})//@Test
  public void withCompression_FileBlank_NoRestart_2() throws IOException {
    defaultTest("test2", "test2", ".gz", FILE_OPTION_BLANK, NO_RESTART);
// END withCompression_FileBlank_NoRestart_2({FormalParametersInternal})//  }
	

    // START noCompression_FileBlank_StopRestart_3({FormalParametersInternal})//@Test
  public void noCompression_FileBlank_StopRestart_3() throws IOException {
    defaultTest("test3", "test3", "", FILE_OPTION_BLANK, WITH_RESTART);
// END noCompression_FileBlank_StopRestart_3({FormalParametersInternal})//  }
	

    // START noCompression_FileSet_StopRestart_4({FormalParametersInternal})//@Test
  public void noCompression_FileSet_StopRestart_4() throws IOException {
    defaultTest("test4", "test4", "", FILE_OPTION_SET, WITH_RESTART);
// END noCompression_FileSet_StopRestart_4({FormalParametersInternal})//  }
	

    // START noCompression_FileSet_StopRestart_WithLongWait_4B({FormalParametersInternal})//@Test
  public void noCompression_FileSet_StopRestart_WithLongWait_4B() throws IOException {
    defaultTest("test4B", "test4B", "", FILE_OPTION_SET, WITH_RESTART_AND_LONG_WAIT);
// END noCompression_FileSet_StopRestart_WithLongWait_4B({FormalParametersInternal})//  }
	

    // START noCompression_FileSet_NoRestart_5({FormalParametersInternal})//@Test
  public void noCompression_FileSet_NoRestart_5() throws IOException {
    defaultTest("test5", "test6", "", FILE_OPTION_SET, NO_RESTART);
// END noCompression_FileSet_NoRestart_5({FormalParametersInternal})//  }
	

    // START withCompression_FileSet_NoRestart_6({FormalParametersInternal})//@Test
  public void withCompression_FileSet_NoRestart_6() throws IOException {
    defaultTest("test6", "test6", ".gz", FILE_OPTION_SET, NO_RESTART);
// END withCompression_FileSet_NoRestart_6({FormalParametersInternal})//  }
	

    // LOGBACK-168
    // START withMissingTargetDirWithCompression({FormalParametersInternal})//@Test
  public void withMissingTargetDirWithCompression() throws IOException {
    defaultTest("test7", "%d{yyyy-MM-dd, aux}/", ".gz", FILE_OPTION_SET, NO_RESTART);
// END withMissingTargetDirWithCompression({FormalParametersInternal})//  }
	

    // START withMissingTargetDirWithZipCompression({FormalParametersInternal})//@Test
  public void withMissingTargetDirWithZipCompression() throws IOException {
    defaultTest("test8", "%d{yyyy-MM-dd, aux}/", ".zip", FILE_OPTION_SET, NO_RESTART);
// END withMissingTargetDirWithZipCompression({FormalParametersInternal})//  }
	

    // START failed_rename({FormalParametersInternal})//@Test
  public void failed_rename() throws IOException {
    if (!EnvUtilForTests.isWindows())
      return;

    FileOutputStream fos = null;
    try {
      String fileName = testId2FileName("failed_rename");
      File file = new File(fileName);
      file.getParentFile().mkdirs();

      fos = new FileOutputStream(fileName);

      String testId = "failed_rename";
      rolloverChecker = new ZRolloverChecker(testId);
      genericTest(testId, "failed_rename", "", FILE_OPTION_SET, NO_RESTART);


    } finally {
      StatusPrinter.print(context);
      if (fos != null) fos.close();
    }
// END failed_rename({FormalParametersInternal})//  }

}
