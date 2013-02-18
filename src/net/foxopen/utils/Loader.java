package net.foxopen.utils;

import static net.foxopen.utils.Logger.logStderr;
import static net.foxopen.utils.Logger.logStdout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.foxopen.fde.model.FoxModule;
import net.foxopen.fde.model.FoxModule.NotAFoxModuleException;
import net.foxopen.fde.model.abstractObject.AbstractFSItem;
import net.foxopen.fde.model.abstractObject.AbstractModelObject;
import net.foxopen.fde.view.FDEMainWindow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

public class Loader {

  public static IRunnableWithProgress LoadContent(AbstractFSItem target) {
    target.checkFile();
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

    private void refreshUI() {
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          for (Object o : FDEMainWindow.getRoot().getChildren()) {
            AbstractModelObject c = (AbstractModelObject) o;
            c.firePropertyChange("name", null, c.getName());
            c.firePropertyChange("status", null, c.getStatus());
          }
        }
      });
    }

    @Override
    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      monitor.beginTask("Opening " + target.getPath(), IProgressMonitor.UNKNOWN);
      logStdout(nbThreads + " Loader thread started");
      HashMap<String, AbstractFSItem> monitorList = new HashMap<String, AbstractFSItem>();

      synchronized (nbThreads) {
        nbThreads++;
      }
      logStdout(nbThreads + " Loader thread running");
      try {
        // Walk into the root directory
        HashMap<String, AbstractFSItem> dirs = target.readContent();
        monitorList.putAll(dirs);
        for (AbstractFSItem dir : dirs.values()) {
          if (monitor.isCanceled())
            break;
          monitorList.putAll(dir.readContent());
        }
        // Get all modules
        HashMap<String, FoxModule> modules = target.getFoxModules();
        // Parse modules
        monitor.beginTask("Parsing FoxModules", modules.size());
        monitor.subTask("Parsing " + modules.size() + " FoxModules");
        for (FoxModule f : modules.values()) {
          if (monitor.isCanceled())
            break;
          try {
            f.readContent();
          } catch (NotAFoxModuleException e) {
            f.delete();
          }
          monitor.worked(1);
        }
        // Assign a WatchDog
        monitorList.putAll(modules);

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
      refreshUI();
      if (monitor.isCanceled())
        throw new InterruptedException("The long running operation was cancelled");
      try {
        Constants.WATCHDOG = new WatchDog(target.getFile(), monitorList, new WatchDogEventHandler() {

          @Override
          public void modified(AbstractFSItem entry) {
            entry.getParent().firePropertyChange("children", null, entry.getParent().getChildren());
            entry.refreshUI();
          }

          @Override
          public void deleted(AbstractFSItem entry) {
            entry.getParent().getChildren().remove(entry);
            entry.refreshUI();
          }

          @Override
          public void created(AbstractModelObject parent, AbstractFSItem entry) {
            entry.refreshUI();
            parent.refreshUI();
          }
        });
        Constants.WATCHDOG.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }
}
