package net.foxopen.fde.model.abstractObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.HashMap;

import net.foxopen.fde.model.FoxModule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractFSItem extends AbstractModelObject {
  protected Path internalPath;
    
  public AbstractFSItem(String path, AbstractFSItem parent) throws IOException {
    this(parent);
    internalPath = Paths.get(path);
    checkFile();
  }
  
  public AbstractFSItem(Path path, AbstractFSItem parent) throws IOException {
    this(parent);
    internalPath = path;
    checkFile();
  }

  public AbstractFSItem(AbstractFSItem parent) throws IOException {
    this.parent = parent;
  }

  public void open(String path) {
    internalPath = Paths.get(path);
    checkFile();
    clear();
  }

  public String getName() {
    checkFile();
    return internalPath.getFileName() + " " + (isDirty() ? "*" : "");
  }

  public void save() {
    reload();
    checkFile();
    // TODO
  }

  public void reload() {
    try {
      internalPath = Paths.get(internalPath.toFile().getCanonicalPath());
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
    return !internalPath.toFile().canWrite();
  }

  public Image getImage() {
    if (isReadOnly())
      return new Image(Display.getCurrent(), super.getImage(), SWT.IMAGE_DISABLE);
    else
      return super.getImage();
  }

  public String getPath() {
    checkFile();
    return internalPath.toFile().getAbsolutePath().toString();
  }

  public Path getFile() {
    return internalPath;
  }

  public void checkFile() {
    if (internalPath == null)
      throw new IllegalArgumentException("The file system item must be loaded");
    // Can we read the file ?
    if (!internalPath.toFile().canRead()) {
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
  
  abstract public HashMap<String, AbstractFSItem> readContent() throws Exception;

  abstract public HashMap<String, FoxModule> getFoxModules();

}
