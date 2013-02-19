package net.foxopen.foxydocs.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.foxopen.foxydocs.model.FoxModule.NotAFoxModuleException;
import net.foxopen.foxydocs.model.abstractObject.AbstractFSItem;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;
import net.foxopen.utils.Logger;

public class Directory extends AbstractFSItem {

  private final List<AbstractModelObject> contentList = new ArrayList<AbstractModelObject>();

  public Directory(Directory parent) throws IOException {
    super(parent);
  }

  public Directory(Path path, Directory parent) throws IOException {
    super(path, parent);
  }

  public List<AbstractModelObject> getChildren() {
    return contentList;
  }

  public void addChild(AbstractFSItem e) {
    contentList.add(e);
    refreshUI();
  }

  public void walk(Directory entry, Collection<AbstractFSItem> directories) throws IOException {
    for (Path p : Files.newDirectoryStream(entry.internalPath)) {
      BasicFileAttributes attr = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
      if (attr.isDirectory()) {
        Directory d = new Directory(p, this);
        entry.addChild(d);
        directories.add(d);
        //entry.walk(d, directories);
      } else if (attr.isRegularFile()) {
        try {
          FoxModule fox = new FoxModule(p, this);
          entry.addChild(fox);
        } catch (NotAFoxModuleException e) {
          // Nothing
        }
      }
    }
  }

  @Override
  public synchronized Collection<AbstractFSItem> readContent() throws IOException {
    Collection<AbstractFSItem> directories = new ArrayList<AbstractFSItem>();
    checkFile();
    Logger.logStdout("Opening " + getPath());

    // Reset or init the content list
    contentList.clear();
    firePropertyChange("children", null, getChildren());

    // Recursive Walk through directories
    walk(this, directories);
    
    firePropertyChange("children", null, getChildren());

    return directories;
  }

  @Override
  public HashMap<String, FoxModule> getFoxModules() {
    HashMap<String, FoxModule> buffer = new HashMap<String, FoxModule>();
    for (AbstractModelObject e : getChildren()) {
      buffer.putAll(((AbstractFSItem) e).getFoxModules());
    }
    return buffer;
  }

}
