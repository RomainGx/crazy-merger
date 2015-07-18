package com.crazymerger;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class Logger {
  private static final String ERROR_FILE_NAME = "errors";
  private static final String DOUBLON_FILE_NAME = "doublons";
  private static final String CONFLICT_FILE_NAME = "conflicts";

  private File errorLog;
  private File conflictLog;
  private File doublonLog;
  private Charset charset;
  private boolean showConsole;


  public Logger(File logRoot, boolean showConsole, boolean clearPreviousLogs) {
    String timeMark = getTimeMark();

    if (clearPreviousLogs) {
      clearExistingLogFiles(logRoot);
    }

    this.showConsole = showConsole;
    setCharset(StandardCharsets.UTF_8);
    errorLog = new File(logRoot, getFileName(ERROR_FILE_NAME, timeMark));
    doublonLog = new File(logRoot, getFileName(DOUBLON_FILE_NAME, timeMark));
    conflictLog = new File(logRoot, getFileName(CONFLICT_FILE_NAME, timeMark));
  }

  public Logger(File logRoot) {
    this(logRoot, true, true);
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  public void error(String msg) {
    try {
      Files.append(msg + "\n", errorLog, charset);

      if (showConsole) {
        System.err.println(msg);
      }
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void error(String msg, Exception ex) {
    error(msg);

    try {
      FileOutputStream fos = new FileOutputStream(errorLog, true);
      PrintStream printStream = new PrintStream(fos);

      ex.printStackTrace(printStream);
      if (showConsole) {
        ex.printStackTrace();
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void conflict(String msg) {
    try {
      Files.append(msg + "\n", conflictLog, charset);

      if (showConsole) {
        System.out.println(msg);
      }
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void doublon(String msg) {
    try {
      Files.append(msg + "\n", doublonLog, charset);

      if (showConsole) {
        System.out.println(msg);
      }
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void doublon(File file, File existingFile) {
    String msg = file.getAbsolutePath() + " already exists in " + existingFile.getAbsolutePath();
    doublon(msg);
  }

  public void info(String msg) {
    System.out.println(msg);
  }

  private String getFileName(String fileName, String timeMark) {
    return fileName + "-" + timeMark + ".txt";
  }

  private String getTimeMark() {
    Calendar now = Calendar.getInstance();
    StringBuilder timeMark = new StringBuilder(23);

    timeMark.append(now.get(Calendar.YEAR)).append("-")
        .append(now.get(Calendar.MONTH)).append("-")
        .append(now.get(Calendar.DAY_OF_MONTH)).append("-")
        .append(now.get(Calendar.HOUR_OF_DAY)).append("-")
        .append(now.get(Calendar.MINUTE)).append("-")
        .append(now.get(Calendar.SECOND)).append("-")
        .append(now.get(Calendar.MILLISECOND));

    return timeMark.toString();
  }

  private void clearExistingLogFiles(File logRoot) {
    System.out.println("TODO: Clear existing log files"); // TODO Clear existing log files
  }
}
