package com.crazymerger;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * This class logs errors and conflicts (duplicate elements) that happen during merging.
 */
public class Logger {
  private static final String ERROR_FILE_NAME = "errors";
  private static final String CONFLICT_FILE_NAME = "conflicts";

  /** File where to log errors. */
  private File errorLog;
  /** File where to log duplicate elements. */
  private File conflictLog;
  /** Charset used to write in logs. */
  private Charset charset;
  /** Determines whether logs should appear in console (<code>true</code>) or not (<code>false</code>). */
  private boolean showConsole;


  /**
   * Create a new Logger instance.
   * @param logRoot Directory where to write logs.
   * @param showConsole See {@link Logger#showConsole}
   * @param clearPreviousLogs <code>true</code> if previous log files should be removed,
   *                          <code>false</code> otherwise.
   */
  public Logger(File logRoot, boolean showConsole, boolean clearPreviousLogs) {
    String timeMark = getTimeMark();

    if (clearPreviousLogs) {
      clearExistingLogFiles(logRoot);
    }

    this.showConsole = showConsole;
    setCharset(StandardCharsets.UTF_8);
    errorLog = new File(logRoot, getFileName(ERROR_FILE_NAME, timeMark));
    conflictLog = new File(logRoot, getFileName(CONFLICT_FILE_NAME, timeMark));
  }

  public Logger(File logRoot) {
    this(logRoot, true, true);
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  /**
   * @param msg Error message to log.
   */
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

  /**
   * @param msg Error message to log.
   * @param ex Exception to log with the message.
   */
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

  /**
   * @param msg Message to log.
   */
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

  /**
   * @param file File in conflict.
   * @param existingFile Existing file that caused the conflict.
   */
  public void conflict(File file, File existingFile) {
    String msg = file.getAbsolutePath() + " already exists in " + existingFile.getAbsolutePath();
    conflict(msg);
  }

  /**
   * @param msg Message to log.
   */
  public void info(String msg) {
    System.out.println(msg);
  }

  /**
   * @param logName Log name.
   * @param timeMark Time mark used to make the file unique.
   * @return Return the unique file name of the log.
   */
  private String getFileName(String logName, String timeMark) {
    return logName + "-" + timeMark + ".txt";
  }

  /**
   * @return DateTime as a String.
   */
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
