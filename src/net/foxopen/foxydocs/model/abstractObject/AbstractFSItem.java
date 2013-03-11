/*
Copyright (c) 2013, ENERGY DEVELOPMENT UNIT (INFORMATION TECHNOLOGY)
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the DEPARTMENT OF ENERGY AND CLIMATE CHANGE nor the
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
package net.foxopen.foxydocs.model.abstractObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    this(Paths.get(path), parent);
  }

  public AbstractFSItem(Path path, AbstractFSItem parent) {
    this(parent);
    internalPath = path;
    checkFile();
  }

  public AbstractFSItem(AbstractFSItem parent) {
    super(parent);
  }

  public final void open(String path) {
    internalPath = Paths.get(path);
    checkFile();
    clear();
  }

  public final void reload() {
    internalPath = Paths.get(getAbsolutePath());
    checkFile();
  }

  /**
   * Is the file read only ?
   * 
   * @return true if the file is not writable, false otherwise
   * @throws IOException
   */
  public final boolean isReadOnly() {
    return !getFile().canWrite();
  }

  public final String getAbsolutePath() {
    checkFile();
    return getFile().getAbsolutePath().toString();
  }

  public final Path getPath() {
    checkFile();
    return internalPath;
  }

  public final File getFile() {
    return internalPath.toFile();
  }

  public final void checkFile() {
    if (internalPath == null)
      throw new IllegalArgumentException("The file system item must be loaded");
    // Can we read the file ?
    if (!getFile().canRead()) {
      throw new IllegalArgumentException("Cannot read the file");
    }
  }

  @Override
  public Image getImage() {
    if (isReadOnly()) {
      return new Image(Display.getCurrent(), super.getImage(), SWT.IMAGE_DISABLE);
    }
    return super.getImage();
  }

  @Override
  public boolean getHasChildren() {
    return super.getHasChildren() && getChildren().get(0) instanceof AbstractFSItem;
  }

}
