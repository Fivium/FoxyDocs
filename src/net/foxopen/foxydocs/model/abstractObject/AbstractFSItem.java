package net.foxopen.foxydocs.model.abstractObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.Collection;
import java.util.HashMap;

import net.foxopen.foxydocs.model.FoxModule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractFSItem extends AbstractModelObject {
  private Path internalPath;

  abstract public Collection<AbstractFSItem> readContent() throws Exception;

  abstract public HashMap<String, FoxModule> getFoxModules();

  public AbstractFSItem(String path, AbstractFSItem parent) {
    this(parent);
    internalPath = Paths.get(path);
    checkFile();
  }

  public AbstractFSItem(Path path, AbstractFSItem parent) {
    this(parent);
    internalPath = path;
    checkFile();
  }

  public AbstractFSItem(AbstractFSItem parent) {
    super(parent);
  }

  public void open(String path) {
    internalPath = Paths.get(path);
    checkFile();
    clear();
  }

  @Override
  public String getName() {
    checkFile();
    return internalPath.getFileName() + " " + (isDirty() ? "*" : "");
  }

  public void reload() {
    try {
      internalPath = Paths.get(internalPath.toFile().getCanonicalPath());
      checkFile();
      firePropertyChange("status", null, getStatus());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Is the file read only ?
   * 
   * @return true if the file is not writable, false otherwise
   * @throws IOException
   */
  public boolean isReadOnly() {
    checkFile();
    return !getFile().canWrite();
  }
  
  @Override
  public Image getImage() {
    if (isReadOnly()) {
      return new Image(Display.getCurrent(), super.getImage(), SWT.IMAGE_DISABLE);
    }
    return super.getImage();
  }

  public String getAbsolutePath() {
    checkFile();
    return getFile().getAbsolutePath().toString();
  }
  
  public Path getPath(){
    return internalPath;
  }

  public File getFile() {
    return internalPath.toFile();
  }
  
  public void checkFile() {
    if (internalPath == null)
      throw new IllegalArgumentException("The file system item must be loaded");
    // Can we read the file ?
    if (!getFile().canRead()) {
      throw new IllegalArgumentException("Cannot read the file");
    }
  }

  @Override
  public boolean getHasChildren() {
    return super.getHasChildren() && getChildren().get(0) instanceof AbstractFSItem;
  }

  public void sendSignal(WatchEvent<?> event) {
    reload();
  }

}
