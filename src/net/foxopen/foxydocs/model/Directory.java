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
package net.foxopen.foxydocs.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import net.foxopen.foxydocs.model.FoxModule.NotAFoxModuleException;
import net.foxopen.foxydocs.model.abstractObject.AbstractFSItem;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

public class Directory extends AbstractFSItem {

  public Directory(Directory parent) {
    super(parent);
  }

  public Directory(Path path, Directory parent) {
    super(path, parent);
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
    clear();
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
  
  @Override
  public String getName() {
    return getFile().getName();
  }

}
