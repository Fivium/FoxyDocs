package net.foxopen.utils;

import static net.foxopen.utils.Logger.*;

import java.util.HashMap;
import java.util.List;

import net.foxopen.fde.model.FoxModule;
import net.foxopen.fde.model.FoxModule.NotAFoxModuleException;
import net.foxopen.fde.model.abstractObject.AbstractFSItem;

public class Loader {

  public static void LoadContent(AbstractFSItem target) throws Exception {
    target.checkFile();
    new ThreadPopulateStructure(target);
  }

  private static class ThreadPopulateStructure extends Thread {
    private final AbstractFSItem target;
    private static Integer nbThreads = 0;
    private static HashMap<String, Boolean> doneList = new HashMap<String, Boolean>();

    public ThreadPopulateStructure(AbstractFSItem target) {
      this.target = target;
      if (!doneList.containsKey(target.getPath())) {
        doneList.put(target.getPath(), true);
        this.start();
      } else {
        logStderr(target.getPath() + " has already been scanned");
      }
    }

    public void run() {
      logStdout(nbThreads + " Loader thread started");
      synchronized (nbThreads) {
        nbThreads++;
      }
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
        logStderr("Failed to load " + target.getPath());
        logStderr(e.getMessage());
      }

      logStdout(nbThreads + " Loader thread done");
      synchronized (nbThreads) {
        nbThreads--;
      }
    }

  }
}
