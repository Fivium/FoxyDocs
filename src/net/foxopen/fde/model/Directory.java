package net.foxopen.fde.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.foxopen.fde.model.abstractObject.AbstractFSItem;
import net.foxopen.utils.Logger;

public class Directory extends AbstractFSItem {

  private final List<AbstractFSItem> contentList = new ArrayList<AbstractFSItem>();

  public Directory(Directory parent) throws IOException {
    super(parent);
  }

  public Directory(String path, Directory parent) throws IOException {
    super(path, parent);
  }

  public List<AbstractFSItem> getChildren() {
    return contentList;
  }

  public void addChild(AbstractFSItem e) {
    contentList.add(e);
    firePropertyChange("children", null, getChildren());
  }

  @Override
  public synchronized HashMap<String, AbstractFSItem> readContent() throws Exception {
    HashMap<String, AbstractFSItem> directories = new HashMap<String, AbstractFSItem>();
    checkFile();
    Logger.logStdout("Opening " + getPath());
    String files;

    // Reset or init the content list
    contentList.clear();
    firePropertyChange("children", null, getChildren());

    // Populate stuff
    for (File file : f_file.toFile().listFiles()) {
      files = file.getName().toUpperCase();
      if (file.isFile()) {
        if (files.endsWith(".XML")) {
          addChild(new FoxModule(file.getPath(), this));
        }
      } else if (file.isDirectory() && !files.startsWith(".")) {
        Directory d = new Directory(file.getPath(), this);
        addChild(d);
        directories.put(file.getPath(),d);
      }
    }
    return directories;
  }

  @Override
  public HashMap<String,FoxModule> getFoxModules() {
    HashMap<String,FoxModule> buffer = new HashMap<String,FoxModule>();
    for (AbstractFSItem e : getChildren()) {
      buffer.putAll(e.getFoxModules());
    }
    return buffer;
  }
}
