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

public class Directory extends AbstractFSItem {

  private final List<AbstractModelObject> contentList = new ArrayList<AbstractModelObject>();

  public Directory(Directory parent) {
    super(parent);
  }

  public Directory(Path path, Directory parent) {
    super(path, parent);
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    return contentList;
  }

  public void addChild(AbstractFSItem e) {
    contentList.add(e);
    refreshUI();
  }

  public void walk(Directory entry, Collection<AbstractFSItem> directories) throws IOException {
    for (Path path : Files.newDirectoryStream(entry.getPath())) {
      BasicFileAttributes attr = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
      // Don't like links...
      if (attr.isSymbolicLink())
        continue;

      // Don't like hidden files...
      if (path.toFile().isHidden())
        continue;

      // Directory
      if (attr.isDirectory()) {
        Directory d = new Directory(path, this);
        entry.addChild(d);
        directories.add(d);
      }
      // File
      else if (attr.isRegularFile()) {
        // Parse only XML's
        String type = Files.probeContentType(path);
        if (type == null || !type.endsWith("xml"))
          continue;

        try {
          FoxModule fox = new FoxModule(path, this);
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
