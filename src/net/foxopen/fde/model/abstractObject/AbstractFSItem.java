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
  protected Path f_file;
    
  public AbstractFSItem(String path, AbstractFSItem parent) throws IOException {
    this(parent);
    f_file = Paths.get(path);
    checkFile();
  }
  
  public AbstractFSItem(Path path, AbstractFSItem parent) throws IOException {
    this(parent);
    f_file = path;
    checkFile();
  }

  public AbstractFSItem(AbstractFSItem parent) throws IOException {
    this.parent = parent;
  }

  public void open(String path) {
    f_file = Paths.get(path);
    checkFile();
    clear();
  }

  public String getName() {
    checkFile();
    return f_file.getFileName() + " " + (isDirty() ? "*" : "");
  }

  public void save() {
    checkFile();
    // TODO
  }

  public void reload() {
    try {
      f_file = Paths.get(f_file.toFile().getCanonicalPath());
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
    return !f_file.toFile().canWrite();
  }

  public Image getImage() {
    if (isReadOnly())
      return new Image(Display.getCurrent(), super.getImage(), SWT.IMAGE_DISABLE);
    else
      return super.getImage();
  }

  public String getPath() {
    checkFile();
    return f_file.toFile().getAbsolutePath().toString();
  }

  public Path getFile() {
    return f_file;
  }

  public void checkFile() {
    if (f_file == null)
      throw new IllegalArgumentException("The file system item must be loaded");
    // Can we read the file ?
    if (!f_file.toFile().canRead()) {
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
