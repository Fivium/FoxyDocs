package net.foxopen.fde.model.abstractObject;

import java.io.File;
import java.util.List;

import net.foxopen.fde.model.FoxModule;

public abstract class AbstractFSItem extends AbstractModelObject {
  protected File f_file;

  public AbstractFSItem(File file, AbstractFSItem parent) {
    this.parent = parent;
    f_file = file;
  }

  public AbstractFSItem(AbstractFSItem parent) {
    this.parent = parent;
  }

  public void open(String path) {
    f_file = new File(path);
    checkFile();
  }

  public String getName() {
    checkFile();
    return f_file.getName();
  }

  public String getPath() {
    checkFile();
    return f_file.getPath();
  }

  public void refresh() throws Exception {
    checkFile();
    readContent();
  }

  public void checkFile() {
    if (f_file == null)
      throw new IllegalArgumentException("The file system item must be loaded");
    // Can we read the file ?
    if (!f_file.canRead()) {
      throw new IllegalArgumentException("Cannot read " + f_file.getAbsolutePath());
    }
  }
  
  @Override
  public boolean getHasChildren() {
    return super.getHasChildren() && getChildren().get(0) instanceof AbstractFSItem;
  }

  abstract public void readContent() throws Exception;
  
  abstract public List<FoxModule> getFoxModules();

}
