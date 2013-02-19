package net.foxopen.utils;

import static net.foxopen.utils.Logger.logStderr;
import static net.foxopen.FoxyDocs.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.foxopen.foxydocs.model.FoxModule;
import net.foxopen.foxydocs.model.FoxModule.NotAFoxModuleException;
import net.foxopen.foxydocs.model.abstractObject.AbstractFSItem;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;
import net.foxopen.foxydocs.view.FDEMainWindow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

public class Loader {

  public static IRunnableWithProgress LoadContent(AbstractFSItem target) {
    target.checkFile();
    return new ThreadPopulateStructure(target);
  }

  private static class ThreadPopulateStructure implements IRunnableWithProgress {
    private final AbstractFSItem target;
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
      final HashMap<String, AbstractFSItem> monitorList = new HashMap<String, AbstractFSItem>();

      try {
        // Walk into the root directory
        ConcurrentLinkedQueue<AbstractFSItem> directories = new ConcurrentLinkedQueue<AbstractFSItem>();
        directories.addAll(target.readContent());
        while (!directories.isEmpty()) {
          directories.addAll(directories.poll().readContent());
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
        MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", e.getMessage());
        e.printStackTrace();
      }

      monitor.done();
      refreshUI();
      if (monitor.isCanceled())
        throw new InterruptedException("The long running operation was cancelled");
      try {
        WATCHDOG = new WatchDog(target.getFile(), monitorList, new WatchDogEventHandler() {

          @Override
          public void modified(AbstractFSItem entry) {
            entry.getParent().firePropertyChange("children", null, entry.getParent().getChildren());
            entry.refreshUI();
          }

          @Override
          public void deleted(AbstractFSItem entry) {
            entry.getParent().getChildren().remove(entry);
            entry.refreshUI();
            monitorList.remove(entry);
          }

          @Override
          public void created(AbstractFSItem parent, Path entry) {
            try {
              FoxModule fox = new FoxModule(entry, parent);
              parent.getChildren().add(fox);
              parent.refreshUI();
            } catch (IOException e) {
              e.printStackTrace();
            } catch (NotAFoxModuleException e) {
              e.printStackTrace();
            }

          }
        });
        WATCHDOG.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
