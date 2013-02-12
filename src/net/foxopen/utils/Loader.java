package net.foxopen.utils;

import static net.foxopen.utils.Logger.logStdout;

import java.util.List;

import net.foxopen.fde.model.Directory;
import net.foxopen.fde.model.FoxModule;
import net.foxopen.fde.model.FoxModule.NotAFoxModuleException;
import net.foxopen.fde.model.abstractObject.AbstractFSItem;

public class Loader {

  private static final Object mutex = new Object();

  public static void LoadContent(AbstractFSItem target) throws Exception {
    target.checkFile();
    Thread threadLoader = new ThreadLoader(target);
    threadLoader.start();
  }

  private static class ThreadLoader extends Thread {
    private final AbstractFSItem target;
    private static Integer nbThreads = 0;

    public ThreadLoader(AbstractFSItem target) {
      this.target = target;
    }

    public void run() {
      logStdout(nbThreads + " Loader thread started");
      synchronized (nbThreads) {
        nbThreads++;
      }
      synchronized (mutex) {
        logStdout(nbThreads + " Loader thread running");
        try {
          target.readContent();
          List<FoxModule> modules = target.getFoxModules();
          logStdout(modules.size() + " Fox Modules");
          for (FoxModule f : modules) {
            try {
              f.readContent();
            } catch (NotAFoxModuleException e) {
              f.delete();
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          Logger.logStderr("Failed to load " + target.getPath());
          Logger.logStderr(e.getMessage());
        }
      }
      logStdout(nbThreads + " Loader thread done");
      synchronized (nbThreads) {
        nbThreads--;
      }
    }
  }

}
