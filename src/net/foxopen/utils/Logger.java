package net.foxopen.utils;

import java.io.*;

/**
 * @author Pierre-Dominique Putallaz
 */
public final class Logger {

  public static void logStdout(String message) {
    write(message, System.out);
  }

  public synchronized static void logStderr(String message) {
    write(message, System.err);
  }

  private synchronized static void write(String message, PrintStream out) {
    int callerPos = 3;
    StackTraceElement caller = Thread.currentThread().getStackTrace()[callerPos];
    out.println("[" + caller.getFileName() + ":" + caller.getLineNumber() + "] " + message);
  }
}
