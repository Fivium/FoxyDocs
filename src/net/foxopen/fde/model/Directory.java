package net.foxopen.fde.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.foxopen.fde.model.abstractObject.AbstractFSItem;
import net.foxopen.utils.Logger;

public class Directory extends AbstractFSItem {

  private List<AbstractFSItem> contentList;

  public Directory(Directory parent) {
    super(parent);
  }

  public Directory(File file, Directory parent) {
    super(file, parent);
  }

  public List<AbstractFSItem> getChildren() {
    return contentList;
  }

  public void addChild(AbstractFSItem e) {
    contentList.add(e);
    cascadeFirePropertyChange("children", null, getChildren());
  }

  @Override
  public synchronized void readContent() throws Exception {
    checkFile();
    Logger.logStdout("Opening " + getPath());
    String files;
    if (f_file.isFile()) {
      throw new IllegalArgumentException("You cannot load just one file : " + getPath());
    }
    File[] listOfFiles = f_file.listFiles();
    Logger.logStdout("Items : " + listOfFiles.length);

    // Reset or init the content list
    contentList = new ArrayList<AbstractFSItem>();
    cascadeFirePropertyChange("children", null, getChildren());
    
    // Populate stuff
    for (File file : listOfFiles) {
      files = file.getName().toUpperCase();
      if (file.isFile()) {
        if (files.endsWith(".XML")) {
          addChild(new FoxModule(file, this));
        }
      } else if (file.isDirectory() && !files.startsWith(".")) {
        Directory d = new Directory(file, this);
        d.readContent();
        addChild(d);
      }
    }
  }
 
  @Override
  public List<FoxModule> getFoxModules() {
    List<FoxModule> buffer = new ArrayList<FoxModule>();
    for (AbstractFSItem e : getChildren()) {
      buffer.addAll(e.getFoxModules());
    }
    return buffer;
  }

}
