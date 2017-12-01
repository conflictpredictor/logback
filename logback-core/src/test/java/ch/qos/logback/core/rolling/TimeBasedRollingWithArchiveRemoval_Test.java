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

 
 
 

import ch.qos.logback.core.rolling.helper.RollingCalendar;
 
 
 
import ch.qos.logback.core.util.StatusPrinter;
 

import org.junit.Before;
 
import org.junit.Test;
 

import java.io.File;
 
import java.io.FileFilter;
 
 
 
import java.util.regex.Matcher;
 
import java.util.regex.Pattern;
 

import static ch.qos.logback.core.CoreConstants.DAILY_DATE_PATTERN;
 
import static org.junit.Assert.assertEquals;
 
import static org.junit.Assert.assertTrue;
 
import java.util.ArrayList; 
import java.util.Calendar; 
import java.util.Date; 
import java.util.HashSet; 
import java.util.List; 
import java.util.Set; 

public
  class
  TimeBasedRollingWithArchiveRemoval_Test  extends ScaffoldingForRollingTests
 {
	
    String MONTHLY_DATE_PATTERN = "yyyy-MM";

	
    String MONTHLY_CRONOLOG_DATE_PATTERN = "yyyy/MM";

	
    final String DAILY_CRONOLOG_DATE_PATTERN = "yyyy/MM/dd";

	

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();

	
    TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();

	

    // by default tbfnatp is an instance of DefaultTimeBasedFileNamingAndTriggeringPolicy
    TimeBasedFileNamingAndTriggeringPolicy<Object> tbfnatp = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();

	

    long MILLIS_IN_MINUTE = 60 * 1000;

	
    long MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTE;

	
    long MILLIS_IN_DAY = 24 * MILLIS_IN_HOUR;

	
    long MILLIS_IN_MONTH = (long) ((365.242199 / 12) * MILLIS_IN_DAY);

	
    int MONTHS_IN_YEAR = 12;

	

    int slashCount = 0;

	

    // START setUp({FormalParametersInternal})//@Before
  public void setUp() {
    super.setUp();
// END setUp({FormalParametersInternal})//  }
	

    // START computeSlashCount(String-String)//private int computeSlashCount(String datePattern) {
    if (datePattern == null)
      return 0;
    else {
      int count = 0;
      for (int i = 0; i < datePattern.length(); i++) {
        char c = datePattern.charAt(i);
        if (c == '/')
          count++;
      }
      return count;
    }
// END computeSlashCount(String-String)//  }
	

    // test that the number of files at the end of the test is same as the expected number taking into account end dates
    // near the beginning of a new year. This test has been run in a loop with start date varying over a two years
    // with success.
    // START monthlyRolloverOverManyPeriods({FormalParametersInternal})//@Test
  public void monthlyRolloverOverManyPeriods() {

    slashCount = computeSlashCount(MONTHLY_CRONOLOG_DATE_PATTERN);
    int numPeriods = 40;
    int maxHistory = 2;
    String fileNamePattern = randomOutputDir + "/%d{" + MONTHLY_CRONOLOG_DATE_PATTERN + "}/clean.txt.zip";

    long startTime = currentTime;
    long endTime = logOverMultiplePeriodsContinuously(currentTime, fileNamePattern, MILLIS_IN_MONTH, maxHistory,
            numPeriods);
    System.out.println("randomOutputDir:" + randomOutputDir);
    System.out.println("start:" + startTime + ", end=" + endTime);
    int differenceInMonths = RollingCalendar.diffInMonths(startTime, endTime);
    System.out.println("differenceInMonths:" + differenceInMonths);
    Calendar startTimeAsCalendar = Calendar.getInstance();
    startTimeAsCalendar.setTimeInMillis(startTime);
    int indexOfStartPeriod = startTimeAsCalendar.get(Calendar.MONTH);
    boolean withExtraFolder = extraFolder(differenceInMonths, MONTHS_IN_YEAR, indexOfStartPeriod, maxHistory);

    check(expectedCountWithFolders(maxHistory, withExtraFolder));
// END monthlyRolloverOverManyPeriods({FormalParametersInternal})//  }
	

    // START generateDailyRollover(long-long-int-int-int-int-int-int-int-int)//void generateDailyRollover(long now, int maxHistory, int simulatedNumberOfPeriods, int startInactivity,
                             int numInactivityPeriods) {
    slashCount = computeSlashCount(DAILY_DATE_PATTERN);
    logOverMultiplePeriods(now, randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt", MILLIS_IN_DAY, maxHistory, simulatedNumberOfPeriods, startInactivity, numInactivityPeriods);
    check(expectedCountWithoutFoldersWithInactivity(maxHistory, simulatedNumberOfPeriods, startInactivity + numInactivityPeriods));
// END generateDailyRollover(long-long-int-int-int-int-int-int-int-int)//  }
	

    // START basicDailyRollover({FormalParametersInternal})//@Test
  public void basicDailyRollover() {
    int maxHistory = 20;
    int simulatedNumberOfPeriods = 20 * 3;
    int startInactivity = 0;
    int numInactivityPeriods = 0;
    generateDailyRollover(currentTime, maxHistory, simulatedNumberOfPeriods, startInactivity, numInactivityPeriods);
// END basicDailyRollover({FormalParametersInternal})//  }
	

    // Since the duration of a month (in seconds) varies from month to month, tests with inactivity period must
    // be conducted with daily rollover not monthly
    // START dailyRollover15({FormalParametersInternal})//@Test
  public void dailyRollover15() {
    int maxHistory = 5;
    int simulatedNumberOfPeriods = 15;
    int startInactivity = 6;
    int numInactivityPeriods = 3;
    generateDailyRollover(currentTime, maxHistory, simulatedNumberOfPeriods, startInactivity, numInactivityPeriods);
// END dailyRollover15({FormalParametersInternal})//  }
	

    // START dailyRolloverWithInactivity70({FormalParametersInternal})//@Test
  public void dailyRolloverWithInactivity70() {
    int maxHistory = 6;
    int simulatedNumberOfPeriods = 70;
    int startInactivity = 30;
    int numInactivityPeriods = 1;
    generateDailyRollover(currentTime, maxHistory, simulatedNumberOfPeriods, startInactivity, numInactivityPeriods);
// END dailyRolloverWithInactivity70({FormalParametersInternal})//  }
	

    // START dailyRolloverWithInactivity10({FormalParametersInternal})//@Test
  public void dailyRolloverWithInactivity10() {
    int maxHistory = 6;
    int simulatedNumberOfPeriods = 10;
    int startInactivity = 3;
    int numInactivityPeriods = 4;
    generateDailyRollover(currentTime, maxHistory, simulatedNumberOfPeriods, startInactivity, numInactivityPeriods);
// END dailyRolloverWithInactivity10({FormalParametersInternal})//  }
	

    // START dailyRolloverWithSecondPhase({FormalParametersInternal})//@Test
  public void dailyRolloverWithSecondPhase() {
    slashCount = computeSlashCount(DAILY_DATE_PATTERN);
    int maxHistory = 5;
    long endTime = logOverMultiplePeriodsContinuously(currentTime, randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt",
            MILLIS_IN_DAY, maxHistory, maxHistory * 2);
    logOverMultiplePeriodsContinuously(endTime + MILLIS_IN_DAY * 10, randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt",
            MILLIS_IN_DAY, maxHistory, maxHistory);
    check(expectedCountWithoutFolders(maxHistory));
// END dailyRolloverWithSecondPhase({FormalParametersInternal})//  }
	

    // START dailyCronologRollover({FormalParametersInternal})//@Test
  public void dailyCronologRollover() {
    slashCount = computeSlashCount(DAILY_CRONOLOG_DATE_PATTERN);
    logOverMultiplePeriodsContinuously(currentTime, randomOutputDir + "/%d{" + DAILY_CRONOLOG_DATE_PATTERN + "}/clean.txt.zip",
            MILLIS_IN_DAY, 8, 8 * 3);
    int expectedDirMin = 9 + slashCount;
    int expectDirMax = expectedDirMin + 1 + 1;
    expectedFileAndDirCount(9, expectedDirMin, expectDirMax);
// END dailyCronologRollover({FormalParametersInternal})//  }
	

    // START dailySizeBasedRollover({FormalParametersInternal})//@Test
  public void dailySizeBasedRollover() {
    SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<Object>();
    sizeAndTimeBasedFNATP.setMaxFileSize("10000");
    tbfnatp = sizeAndTimeBasedFNATP;
    slashCount = computeSlashCount(DAILY_DATE_PATTERN);
    logOverMultiplePeriodsContinuously(currentTime, randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}-clean.%i.zip", MILLIS_IN_DAY,
            5, 5 * 4);
    checkPatternCompliance(5 + 1 + slashCount, "\\d{4}-\\d{2}-\\d{2}-clean(\\.\\d)(.zip)?");
// END dailySizeBasedRollover({FormalParametersInternal})//  }
	

    // START dailyChronologSizeBasedRollover({FormalParametersInternal})//@Test
  public void dailyChronologSizeBasedRollover() {
    SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<Object>();
    sizeAndTimeBasedFNATP.setMaxFileSize("10000");
    tbfnatp = sizeAndTimeBasedFNATP;
    slashCount = 1;
    logOverMultiplePeriodsContinuously(currentTime, randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i.zip", MILLIS_IN_DAY,
            5, 5 * 4);
    checkDirPatternCompliance(6);
// END dailyChronologSizeBasedRollover({FormalParametersInternal})//  }
	

    // START dailyChronologSizeBasedRolloverWithSecondPhase({FormalParametersInternal})//@Test
  public void dailyChronologSizeBasedRolloverWithSecondPhase() {
    SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<Object>();
    sizeAndTimeBasedFNATP.setMaxFileSize("10000");
    tbfnatp = sizeAndTimeBasedFNATP;
    slashCount = 1;
    int maxHistory = 5;
    int simulatedNumberOfPeriods = maxHistory * 4;
    long endTime = logOverMultiplePeriodsContinuously(currentTime, randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i", MILLIS_IN_DAY,
            maxHistory, 3);
    logOverMultiplePeriodsContinuously(endTime + MILLIS_IN_DAY * 7, randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i",
            MILLIS_IN_DAY, maxHistory, simulatedNumberOfPeriods);
    checkDirPatternCompliance(maxHistory + 1);
// END dailyChronologSizeBasedRolloverWithSecondPhase({FormalParametersInternal})//  }
	

    // START logOncePeriod(long-long-String-String-int-int)//void logOncePeriod(long currentTime, String fileNamePattern, int maxHistory) {
    buildRollingFileAppender(currentTime, fileNamePattern, maxHistory, DO_CLEAN_HISTORY_ON_START);
    rfa.doAppend("Hello ----------------------------------------------------------" + new Date(currentTime));
    rfa.stop();
// END logOncePeriod(long-long-String-String-int-int)//  }
	

    @Test
    public void cleanHistoryOnStart() {
        long now = this.currentTime;
        String fileNamePattern = randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt";
        int maxHistory = 3;
        for (int i = 0; i <= 5; i++) {
            logOncePeriod(now, fileNamePattern, maxHistory);
            now = now + MILLIS_IN_DAY;
        }
        // StatusPrinter.print(context);
        check(expectedCountWithoutFolders(maxHistory));
    }
	

    @Test
    public void cleanHistoryOnStartWithDayPattern() {
        long now = this.currentTime;
        String fileNamePattern = randomOutputDir + "clean-%d{yyyy-MM-dd}.txt";
        int maxHistory = 3;
        for (int i = 0; i <= 5; i++) {
            logOncePeriod(now, fileNamePattern, maxHistory);
            now = now + MILLIS_IN_DAY;
        }
        StatusPrinter.print(context);
        check(expectedCountWithoutFolders(maxHistory));
    }
	

    @Test
    public void cleanHistoryOnStartWithHourPattern() {
        long now = this.currentTime;
        String fileNamePattern = randomOutputDir + "clean-%d{HH}.txt";
        int maxHistory = 3;
        for (int i = 0; i <= 5; i++) {
            logOncePeriod(now, fileNamePattern, maxHistory);
            now = now + MILLIS_IN_HOUR;
        }
        // StatusPrinter.print(context);
        check(expectedCountWithoutFolders(maxHistory));
    }
	

    // START expectedCountWithoutFolders(int-int)//int expectedCountWithoutFolders(int maxHistory) {
    return maxHistory + 1;
// END expectedCountWithoutFolders(int-int)//  }
	

    // START expectedCountWithFolders(int-int-boolean-boolean)//int expectedCountWithFolders(int maxHistory, boolean withExtraFolder) {
    int numLogFiles = (maxHistory + 1);
    int numLogFilesAndFolders = numLogFiles * 2;
    int result = numLogFilesAndFolders + slashCount;
    if (withExtraFolder) result += 1;
    return result;
// END expectedCountWithFolders(int-int-boolean-boolean)//  }
	

    // START buildRollingFileAppender(long-long-String-String-int-int-boolean-boolean)//void buildRollingFileAppender(long currentTime, String fileNamePattern, int maxHistory,
                                boolean cleanHistoryOnStart) {
    rfa.setContext(context);
    rfa.setEncoder(encoder);
    tbrp.setContext(context);
    tbrp.setFileNamePattern(fileNamePattern);
    tbrp.setMaxHistory(maxHistory);
    tbrp.setParent(rfa);
    tbrp.setCleanHistoryOnStart(cleanHistoryOnStart);
    tbrp.timeBasedFileNamingAndTriggeringPolicy = tbfnatp;
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();
// END buildRollingFileAppender(long-long-String-String-int-int-boolean-boolean)//  }
	

    boolean DO_CLEAN_HISTORY_ON_START = true;

	
    boolean DO_NOT_CLEAN_HISTORY_ON_START = false;

	

    // START logOverMultiplePeriodsContinuously(long-long-String-String-long-long-int-int-int-int)//long logOverMultiplePeriodsContinuously(long simulatedTime, String fileNamePattern, long periodDurationInMillis, int maxHistory,
                                          int simulatedNumberOfPeriods) {
    return logOverMultiplePeriods(simulatedTime, fileNamePattern, periodDurationInMillis, maxHistory,
            simulatedNumberOfPeriods, 0, 0);
// END logOverMultiplePeriodsContinuously(long-long-String-String-long-long-int-int-int-int)//  }
	

    // START logOverMultiplePeriods(long-long-String-String-long-long-int-int-int-int-int-int-int-int)//long logOverMultiplePeriods(long simulatedTime, String fileNamePattern, long periodDurationInMillis, int maxHistory,
                              int simulatedNumberOfPeriods, int startInactivity,
                              int numInactivityPeriods) {
    buildRollingFileAppender(simulatedTime, fileNamePattern, maxHistory, DO_NOT_CLEAN_HISTORY_ON_START);
    int ticksPerPeriod = 512;
    int runLength = simulatedNumberOfPeriods * ticksPerPeriod;
    int startInactivityIndex = 1 + startInactivity * ticksPerPeriod;
    int endInactivityIndex = startInactivityIndex + numInactivityPeriods * ticksPerPeriod;
    long tickDuration = periodDurationInMillis / ticksPerPeriod;

    for (int i = 0; i <= runLength; i++) {
      if (i < startInactivityIndex || i > endInactivityIndex) {
        rfa.doAppend("Hello ----------------------------------------------------------" + i);
      }
      tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(addTime(tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime(),
              tickDuration));
      add(tbrp.future);
      waitForJobsToComplete();
    }
    rfa.stop();
    return tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime();
// END logOverMultiplePeriods(long-long-String-String-long-long-int-int-int-int-int-int-int-int)//  }
	

    // START extraFolder(int-int-int-int-int-int-int-int)//boolean extraFolder(int numPeriods, int periodsPerEra, int beginPeriod, int maxHistory) {
    int valueOfLastMonth = ((beginPeriod) + numPeriods) % periodsPerEra;
    return (valueOfLastMonth < maxHistory);
// END extraFolder(int-int-int-int-int-int-int-int)//  }
	

    // START addTime(long-long-long-long)//long addTime(long time, long timeToWait) {
    return time + timeToWait;
// END addTime(long-long-long-long)//  }
	

    // START expectedFileAndDirCount(int-int-int-int-int-int)//void expectedFileAndDirCount(int expectedFileAndDirCount, int expectedDirCountMin, int expectedDirCountMax) {
    File dir = new File(randomOutputDir);
    List<File> fileList = new ArrayList<File>();
    findFilesInFolderRecursivelyByPatterMatch(dir, fileList, "clean");
    List<File> dirList = new ArrayList<File>();
    findAllFoldersInFolderRecursively(dir, dirList);
    String msg = "expectedDirCountMin=" + expectedDirCountMin + ", expectedDirCountMax=" + expectedDirCountMax + " actual value=" + dirList.size();
    assertTrue(msg, expectedDirCountMin <= dirList.size() && dirList.size() <= expectedDirCountMax);
// END expectedFileAndDirCount(int-int-int-int-int-int)//  }
	

    // START check(int-int)//void check(int expectedCount) {
    File dir = new File(randomOutputDir);
    List<File> fileList = new ArrayList<File>();
    findAllDirsOrStringContainsFilesRecursively(dir, fileList, "clean");
    assertEquals(expectedCount, fileList.size());
// END check(int-int)//  }
	

    // START expectedCountWithoutFoldersWithInactivity(int-int-int-int-int-int)//int expectedCountWithoutFoldersWithInactivity(int maxHistory, int totalPeriods, int endOfInactivity) {
    int availableHistory = (totalPeriods + 1) - endOfInactivity;
    int actualHistory = Math.min(availableHistory, maxHistory + 1);
    return actualHistory;
// END expectedCountWithoutFoldersWithInactivity(int-int-int-int-int-int)//  }
	

    // START genericFindMatching(FileMatchFunction-FileMatchFunction-File-File-List<File>-List<File>-String-String-boolean-boolean)//void genericFindMatching(final FileMatchFunction matchFunc, File dir, List<File> fileList, final String pattern, boolean includeDirs) {
    if (dir.isDirectory()) {
      File[] matchArray = dir.listFiles(new FileFilter() {
        public boolean accept(File f) {
          return f.isDirectory() || matchFunc.match(f, pattern);
        }
      });
      for (File f : matchArray) {
        if (f.isDirectory()) {
          if (includeDirs) fileList.add(f);
          genericFindMatching(matchFunc, f, fileList, pattern, includeDirs);
        } else
          fileList.add(f);
      }
    }
// END genericFindMatching(FileMatchFunction-FileMatchFunction-File-File-List<File>-List<File>-String-String-boolean-boolean)//  }
	

    // START findAllFoldersInFolderRecursively(File-File-List<File>-List<File>)//private void findAllFoldersInFolderRecursively(File dir, List<File> fileList) {
    FileMatchFunction alwaysFalse = new FileMatchFunction() {
      public boolean match(File f, String pattern) {
        return false;
      }
    };
    genericFindMatching(alwaysFalse, dir, fileList, null, true);
// END findAllFoldersInFolderRecursively(File-File-List<File>-List<File>)//  }
	

    // START findAllDirsOrStringContainsFilesRecursively(File-File-List<File>-List<File>-String-String)//private void findAllDirsOrStringContainsFilesRecursively(File dir, List<File> fileList, String pattern) {
    FileMatchFunction matchFunction = new FileMatchFunction() {
      public boolean match(File f, String pattern) {
        return f.getName().contains(pattern);
      }
    };
    genericFindMatching(matchFunction, dir, fileList, pattern, true);
// END findAllDirsOrStringContainsFilesRecursively(File-File-List<File>-List<File>-String-String)//  }
	

    // START findFilesInFolderRecursivelyByPatterMatch(File-File-List<File>-List<File>-String-String)//void findFilesInFolderRecursivelyByPatterMatch(File dir, List<File> fileList, String pattern) {
    FileMatchFunction matchByPattern = new FileMatchFunction() {
      public boolean match(File f, String pattern) {
        return f.getName().matches(pattern);
      }
    };
    genericFindMatching(matchByPattern, dir, fileList, pattern, false);
// END findFilesInFolderRecursivelyByPatterMatch(File-File-List<File>-List<File>-String-String)//  }
	

    // START groupByClass(List<File>-List<File>-String-String)//Set<String> groupByClass(List<File> fileList, String regex) {
    Pattern p = Pattern.compile(regex);
    Set<String> set = new HashSet<String>();
    for (File f : fileList) {
      String n = f.getName();
      Matcher m = p.matcher(n);
      m.matches();
      int begin = m.start(1);
      String reduced = n.substring(0, begin);
      set.add(reduced);
    }
    return set;
// END groupByClass(List<File>-List<File>-String-String)//  }
	

    // START checkPatternCompliance(int-int-String-String)//void checkPatternCompliance(int expectedClassCount, String regex) {
    File dir = new File(randomOutputDir);
    List<File> fileList = new ArrayList<File>();
    findFilesInFolderRecursivelyByPatterMatch(dir, fileList, regex);
    Set<String> set = groupByClass(fileList, regex);
    assertEquals(expectedClassCount, set.size());
// END checkPatternCompliance(int-int-String-String)//  }
	

    // START checkDirPatternCompliance(int-int)//void checkDirPatternCompliance(int expectedClassCount) {
    File dir = new File(randomOutputDir);
    List<File> fileList = new ArrayList<File>();
    findAllFoldersInFolderRecursively(dir, fileList);
    for (File f : fileList) {
      assertTrue(f.list().length >= 1);
    }
    assertEquals(expectedClassCount, fileList.size());
// END checkDirPatternCompliance(int-int)//  }

}
