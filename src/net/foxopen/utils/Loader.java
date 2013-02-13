package net.foxopen.utils;

import static net.foxopen.utils.Logger.logStderr;
import static net.foxopen.utils.Logger.logStdout;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import net.foxopen.fde.model.FoxModule;
import net.foxopen.fde.model.FoxModule.NotAFoxModuleException;
import net.foxopen.fde.model.abstractObject.AbstractFSItem;

public class Loader {

  public static IRunnableWithProgress LoadContent(AbstractFSItem target) {
    target.checkFile();
    return new ThreadPopulateStructure(target);
  }

  public static IRunnableWithProgress RefreshContent(AbstractFSItem target) {
    target.checkFile();
    ThreadPopulateStructure.doneList = new HashMap<String, Boolean>();
    return new ThreadPopulateStructure(target);
  }

  private static class ThreadPopulateStructure implements IRunnableWithProgress {
    private final AbstractFSItem target;
    private static Integer nbThreads = 0;
    private static HashMap<String, Boolean> doneList = new HashMap<String, Boolean>();

    public ThreadPopulateStructure(AbstractFSItem target) {
      this.target = target;
      if (!doneList.containsKey(target.getPath())) {
        doneList.put(target.getPath(), true);
      } else {
        logStderr(target.getPath() + " has already been scanned");
      }
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      monitor.beginTask("Opening "+target.getPath(), IProgressMonitor.UNKNOWN);
      logStdout(nbThreads + " Loader thread started");
      synchronized (nbThreads) {
        nbThreads++;
      }
      logStdout(nbThreads + " Loader thread running");
      try {
        target.readContent();
        List<FoxModule> modules = target.getFoxModules();  
        monitor.beginTask("Parsing FoxModules", modules.size());
        monitor.subTask("Parsing FoxModules");
        logStdout(modules.size() + " Fox Modules");
        for (FoxModule f : modules) {
          if (monitor.isCanceled())
            break;
          try {
            f.readContent();
          } catch (NotAFoxModuleException e) {
            f.delete();
          }
         monitor.worked(1);
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
      monitor.done();
      if (monitor.isCanceled())
        throw new InterruptedException("The long running operation was cancelled");

    }

  }
}
