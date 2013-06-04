/*
Copyright (c) 2013, Fivium Ltd.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of Fivium Ltd nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package net.foxopen.foxydocs.utils;

import static net.foxopen.foxydocs.FoxyDocs.*;
import static net.foxopen.foxydocs.utils.Logger.logStderr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.foxopen.foxydocs.FoxyDocs;
import net.foxopen.foxydocs.model.FoxModule;
import net.foxopen.foxydocs.model.FoxModule.NotAFoxModuleException;
import net.foxopen.foxydocs.model.abstractObject.AbstractFSItem;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;
import net.foxopen.foxydocs.view.FoxyDocsMainWindow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;

public class Loader {

  public static IRunnableWithProgress LoadContent(AbstractFSItem target) {
    target.checkFile();
    FoxyDocs.stopWatchdog();
    for (CTabItem tab : FoxyDocsMainWindow.tabFolder.getItems()) {
      tab.dispose();
    }
    return new ThreadPopulateStructure(target);
  }

  private static class ThreadPopulateStructure implements IRunnableWithProgress {
    private final AbstractFSItem target;
    private static HashMap<String, Boolean> doneList = new HashMap<String, Boolean>();

    public ThreadPopulateStructure(AbstractFSItem target) {
      this.target = target;
      if (!doneList.containsKey(target.getAbsolutePath())) {
        doneList.put(target.getAbsolutePath(), true);
      } else {
        logStderr(target.getAbsolutePath() + " has already been scanned");
      }
    }

    @Override
    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      monitor.beginTask("Opening " + target.getAbsolutePath(), IProgressMonitor.UNKNOWN);
      HashMap<String, AbstractFSItem> monitorList = new HashMap<String, AbstractFSItem>();
      ConcurrentLinkedQueue<AbstractFSItem> directories = new ConcurrentLinkedQueue<AbstractFSItem>();
      final ArrayList<AbstractFSItem> directoryList = new ArrayList<>();

      try {
        // Walk into the root directory
        directories.add(target);
        do {
          Collection<AbstractFSItem> subDir = directories.poll().readContent();
          directories.addAll(subDir);
          directoryList.addAll(subDir);
        } while (!directories.isEmpty());

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
        throw new InterruptedException(e.getMessage());
      }

      monitor.done();

      Display.getDefault().asyncExec(new Runnable() {
        @Override
        public void run() {
          target.firePropertyChange("status", null, target.getStatus());
          // Copy all children as they may change
          AbstractModelObject[] children = target.getChildren().toArray(new AbstractModelObject[0]);
          for (AbstractModelObject o : children) {
            o.firePropertyChange("status", null, o.getStatus());
          }
        }
      });

      if (monitor.isCanceled()) {
        throw new InterruptedException("The long running operation was cancelled");
      }

      startWatchDog(monitorList);
    }

    private void startWatchDog(final HashMap<String, AbstractFSItem> monitorList) {
      try {
        WATCHDOG = new WatchDog(target.getPath(), new WatchDogEventHandler() {

          private AbstractFSItem resolv(Path path) throws FileNotFoundException {
            AbstractFSItem item = monitorList.get(path.toFile().getAbsolutePath());
            if (item == null)
              throw new FileNotFoundException();
            return item;
          }

          @Override
          public void modified(Path entryPath) {
            if (entryPath != null) {
              try {
                AbstractFSItem targetModule = resolv(entryPath);
                targetModule.readContent();
                targetModule.getParent().firePropertyChange("children", null, targetModule.getParent().getChildren());
                targetModule.refreshUI();
              } catch (FileNotFoundException e) {
                // Nothing
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }

          @Override
          public void deleted(Path entryPath) {
            try {
              resolv(entryPath).getParent().getChildren().remove(resolv(entryPath));
              resolv(entryPath).refreshUI();
              monitorList.remove(resolv(entryPath));
            } catch (FileNotFoundException e) {
              // Nothing
            }
          }

          @Override
          public void created(Path parentPath, Path entryPath) {
            if (parentPath == null || entryPath == null)
              return;
            try {
              FoxModule fox = new FoxModule(entryPath, resolv(parentPath));
              resolv(parentPath).getChildren().add(fox);
              resolv(parentPath).refreshUI();
            } catch (FileNotFoundException e) {
              // Nothing
            } catch (IOException e) {
              e.printStackTrace();
            } catch (NotAFoxModuleException e) {
              // Nothing
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
