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
 
import ch.qos.logback.core.status.InfoStatus;
 
import ch.qos.logback.core.status.StatusManager;
 
 

import org.junit.Before;
 
import org.junit.Test;
 

import java.io.File;
 
import java.io.IOException;
 
import java.util.Date;
 
import java.util.List;
 
import java.util.concurrent.ExecutionException;
 

import static org.junit.Assert.assertFalse; 
import ch.qos.logback.core.status.StatusChecker; 

public
  class
  SizeAndTimeBasedFNATP_Test  extends ScaffoldingForRollingTests
 {
	
    private SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = null;

	
    private RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();

	
    private TimeBasedRollingPolicy<Object> tbrp1 = new TimeBasedRollingPolicy<Object>();

	
    private RollingFileAppender<Object> rfa2 = new RollingFileAppender<Object>();

	
    private TimeBasedRollingPolicy<Object> tbrp2 = new TimeBasedRollingPolicy<Object>();

	

    private EchoEncoder<Object> encoder = new EchoEncoder<Object>();

	
    int fileSize = 0;

	
    int fileIndexCounter = 0;

	
    int sizeThreshold = 0;

	

    // START setUp({FormalParametersInternal})//@Before
  public void setUp() {
    super.setUp();
// END setUp({FormalParametersInternal})//  }
	

    // START initRollingFileAppender(RollingFileAppender<Object>-RollingFileAppender<Object>-String-String)//private void initRollingFileAppender(RollingFileAppender<Object> rfa, String filename) {
    rfa.setContext(context);
    rfa.setEncoder(encoder);
    if (filename != null) {
      rfa.setFile(filename);
    }
// END initRollingFileAppender(RollingFileAppender<Object>-RollingFileAppender<Object>-String-String)//  }
	

    // START initPolicies(RollingFileAppender<Object>-RollingFileAppender<Object>-TimeBasedRollingPolicy<Object>-TimeBasedRollingPolicy<Object>-String-String-int-int-long-long-long-long)//private void initPolicies(RollingFileAppender<Object> rfa,
                            TimeBasedRollingPolicy<Object> tbrp,
                            String filenamePattern, int sizeThreshold,
                            long givenTime, long lastCheck) {
    sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<Object>();
    tbrp.setContext(context);
    sizeAndTimeBasedFNATP.setMaxFileSize("" + sizeThreshold);
    tbrp.setTimeBasedFileNamingAndTriggeringPolicy(sizeAndTimeBasedFNATP);
    tbrp.setFileNamePattern(filenamePattern);
    tbrp.setParent(rfa);
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(givenTime);
    rfa.setRollingPolicy(tbrp);
    tbrp.start();
    rfa.start();
// END initPolicies(RollingFileAppender<Object>-RollingFileAppender<Object>-TimeBasedRollingPolicy<Object>-TimeBasedRollingPolicy<Object>-String-String-int-int-long-long-long-long)//  }
	

    // START addExpectedFileNamedIfItsTime(String-String-String-String-String-String-String-String)//private void addExpectedFileNamedIfItsTime(String randomOutputDir, String testId, String msg, String compressionSuffix) {
    fileSize = fileSize + msg.getBytes().length;
    if (passThresholdTime(nextRolloverThreshold)) {
      fileIndexCounter = 0;
      fileSize = 0;
      addExpectedFileName_ByFileIndexCounter(randomOutputDir, testId, getMillisOfCurrentPeriodsStart(),
              fileIndexCounter, compressionSuffix);
      recomputeRolloverThreshold(currentTime);
      return;
    }

    // windows can delay file size changes, so we only allow for
    // fileIndexCounter 0
    if ((fileIndexCounter < 1) && fileSize > sizeThreshold) {
      addExpectedFileName_ByFileIndexCounter(randomOutputDir, testId, getMillisOfCurrentPeriodsStart(),
              fileIndexCounter, compressionSuffix);
      fileIndexCounter = fileIndexCounter + 1;
      fileSize = 0;
    }
// END addExpectedFileNamedIfItsTime(String-String-String-String-String-String-String-String)//  }
	

    void generic(String testId, String stem, boolean withSecondPhase, String compressionSuffix) throws IOException, InterruptedException, ExecutionException {
        String file = (stem != null) ? randomOutputDir + stem : null;
        initRollingFileAppender(rfa1, file);
        sizeThreshold = 300;
        initPolicies(rfa1, tbrp1, randomOutputDir + testId + "-%d{" + DATE_PATTERN_WITH_SECONDS + "}-%i.txt" + compressionSuffix, sizeThreshold, currentTime, 0);
        addExpectedFileName_ByFileIndexCounter(randomOutputDir, testId, getMillisOfCurrentPeriodsStart(), fileIndexCounter, compressionSuffix);
        incCurrentTime(100);
        tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
        int runLength = 100;
        String prefix = "Hello -----------------";

        for (int i = 0; i < runLength; i++) {
            String msg = prefix + i;
            rfa1.doAppend(msg);
            addExpectedFileNamedIfItsTime(randomOutputDir, testId, msg, compressionSuffix);
            incCurrentTime(20);
            tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
            add(tbrp1.future);
        }

        if (withSecondPhase) {
            secondPhase(testId, file, stem, compressionSuffix, runLength, prefix);
            runLength = runLength * 2;
        }

        if (stem != null)
            massageExpectedFilesToCorresponToCurrentTarget(file, true);

        Thread.yield();
        // wait for compression to finish
        waitForJobsToComplete();

        // StatusPrinter.print(context);
        existenceCheck(expectedFilenameList);
        sortedContentCheck(randomOutputDir, runLength, prefix);
    }
	

    // START secondPhase(String-String-String-String-String-String-String-String-int-int-String-String)//void secondPhase(String testId, String file, String stem, String compressionSuffix, int runLength, String prefix) {
    rfa1.stop();

    if (stem != null) {
      File f = new File(file);
      f.setLastModified(currentTime);
    }

    StatusManager sm = context.getStatusManager();
    sm.add(new InfoStatus("Time when rfa1 is stopped: " + new Date(currentTime), this));
    sm.add(new InfoStatus("currentTime%1000=" + (currentTime % 1000), this));

    initRollingFileAppender(rfa2, file);
    initPolicies(rfa2, tbrp2, randomOutputDir + testId + "-%d{"
            + DATE_PATTERN_WITH_SECONDS + "}-%i.txt" + compressionSuffix, sizeThreshold, currentTime, 0);

    for (int i = runLength; i < runLength * 2; i++) {
      incCurrentTime(100);
      tbrp2.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
      String msg = prefix + i;
      rfa2.doAppend(msg);
      addExpectedFileNamedIfItsTime(randomOutputDir, testId, msg, compressionSuffix);
    }
// END secondPhase(String-String-String-String-String-String-String-String-int-int-String-String)//  }
	

    static final boolean FIRST_PHASE_ONLY = false;

	
    static final boolean WITH_SECOND_PHASE = true;

	
    static String DEFAULT_COMPRESSION_SUFFIX = "";

	

    // START noCompression_FileSet_NoRestart_1({FormalParametersInternal})//@Test
  public void noCompression_FileSet_NoRestart_1() throws InterruptedException, ExecutionException, IOException {
    generic("test1", "toto.log", FIRST_PHASE_ONLY, DEFAULT_COMPRESSION_SUFFIX);
// END noCompression_FileSet_NoRestart_1({FormalParametersInternal})//  }
	

    // START noCompression_FileBlank_NoRestart_2({FormalParametersInternal})//@Test
  public void noCompression_FileBlank_NoRestart_2() throws Exception {
    generic("test2", null, FIRST_PHASE_ONLY, DEFAULT_COMPRESSION_SUFFIX);
// END noCompression_FileBlank_NoRestart_2({FormalParametersInternal})//  }
	

    // START noCompression_FileBlank_WithStopStart_3({FormalParametersInternal})//@Test
  public void noCompression_FileBlank_WithStopStart_3() throws Exception {
    generic("test3", null, WITH_SECOND_PHASE, DEFAULT_COMPRESSION_SUFFIX);
// END noCompression_FileBlank_WithStopStart_3({FormalParametersInternal})//  }
	

    // START noCompression_FileSet_WithStopStart_4({FormalParametersInternal})//@Test
  public void noCompression_FileSet_WithStopStart_4() throws Exception {
    generic("test4", "test4.log", WITH_SECOND_PHASE, DEFAULT_COMPRESSION_SUFFIX);
// END noCompression_FileSet_WithStopStart_4({FormalParametersInternal})//  }
	

    // START withGZCompression_FileSet_NoRestart_5({FormalParametersInternal})//@Test
  public void withGZCompression_FileSet_NoRestart_5() throws Exception {
    generic("test5", "toto.log", FIRST_PHASE_ONLY, ".gz");
// END withGZCompression_FileSet_NoRestart_5({FormalParametersInternal})//  }
	

    // START withGZCompression_FileBlank_NoRestart_6({FormalParametersInternal})//@Test
  public void withGZCompression_FileBlank_NoRestart_6() throws Exception {
    generic("test6", null, FIRST_PHASE_ONLY, ".gz");
// END withGZCompression_FileBlank_NoRestart_6({FormalParametersInternal})//  }
	

    // START withZipCompression_FileSet_NoRestart_7({FormalParametersInternal})//@Test
  public void withZipCompression_FileSet_NoRestart_7() throws Exception {
    generic("test7", "toto.log", FIRST_PHASE_ONLY, ".zip");
    List<String> zipFiles = filterElementsInListBySuffix(".zip");
    checkZipEntryMatchesZipFilename(zipFiles);
// END withZipCompression_FileSet_NoRestart_7({FormalParametersInternal})//  }
	

    @Test
    public void checkMissingIntToken() {
        String stem = "toto.log";
        String testId = "checkMissingIntToken";
        String compressionSuffix = "gz";

        String file = (stem != null) ? randomOutputDir + stem : null;
        initRollingFileAppender(rfa1, file);
        sizeThreshold = 300;
        initPolicies(rfa1, tbrp1, randomOutputDir + testId + "-%d{" + DATE_PATTERN_WITH_SECONDS + "}.txt" + compressionSuffix, sizeThreshold, currentTime, 0);

        // StatusPrinter.print(context);
        assertFalse(rfa1.isStarted());
        StatusChecker checker = new StatusChecker(context);
        checker.assertContainsMatch("Missing integer token");
    }
	

    @Test
    public void checkDateCollision() {
        String stem = "toto.log";
        String testId = "checkDateCollision";
        String compressionSuffix = "gz";

        String file = (stem != null) ? randomOutputDir + stem : null;
        initRollingFileAppender(rfa1, file);
        sizeThreshold = 300;
        initPolicies(rfa1, tbrp1, randomOutputDir + testId + "-%d{EE}.txt" + compressionSuffix, sizeThreshold, currentTime, 0);

        // StatusPrinter.print(context);
        assertFalse(rfa1.isStarted());
        StatusChecker checker = new StatusChecker(context);
        checker.assertContainsMatch("The date format in FileNamePattern");
    }

}
