/*
Copyright (c) 2013, Fivium Ltd.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of Fivium Ltd nor the
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

import static net.foxopen.foxydocs.FoxyDocs.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

import net.foxopen.foxydocs.FoxyDocs;
import net.foxopen.foxydocs.model.ModuleInformation;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractModelObject extends Observable{

  private final List<AbstractModelObject> children = Collections.synchronizedList(new ArrayList<AbstractModelObject>());
  private static Comparator<? super AbstractModelObject> modelComparator;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  private final AbstractModelObject parent;

  protected AbstractModelObject(AbstractModelObject parent) {
    this.parent = parent;

    // Dirty property : does the file need saving ?
    addPropertyChangeListener("dirty", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange("name", null, getName());
        if (getParent() != null) {
          // Cascade propagate the change
          getParent().firePropertyChange("dirty", evt.getOldValue(), evt.getNewValue());
        }
      }
    });

    // Status property
    addPropertyChangeListener("status", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (getStatus() == FoxyDocs.STATUS_UNKNOWN) {
          delete();
        }
        firePropertyChange("name", null, getName());
        if (getParent() != null) {
          // Cascade propagate the change
          getParent().firePropertyChange("status", evt.getOldValue(), evt.getNewValue());
        }
      }
    });
  }

  public final synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  public final synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public final synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  public final synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  public final synchronized void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  public abstract String getName();

  public synchronized final List<AbstractModelObject> getChildren() {
    return children;
  }

  public void save() throws Exception {
    if (!isDirty())
      return;
    for (AbstractModelObject child : getChildren()) {
      child.save();
    }
  }

  public synchronized void addChild(AbstractModelObject child) {
    children.add(child);
    Collections.sort(children, getComparator());
    firePropertyChange("children", null, getChildren());
  }

  public synchronized void delete() {
    if (getParent() != null) {
      getParent().getChildren().remove(this);
      getParent().firePropertyChange("children", null, getParent().getChildren());
    }
  }

  public synchronized int getStatus() {
    int status = STATUS_UNKNOWN;
    for (AbstractModelObject child : getChildren()) {
      status |= child.getStatus();
    }
    return status;
  }

  public final void clear() {
    if (getHasChildren()) {
      for (AbstractModelObject child : getChildren()) {
        child.clear();
      }
      getChildren().clear();
      firePropertyChange("children", null, getChildren());
    }
  }

  public boolean getHasChildren() {
    return getChildren() != null && !getChildren().isEmpty();
  }

  public String getCode() {
    throw new RuntimeException("You cannot call this method directly. Please override it.");
  }

  public final AbstractModelObject getParent() {
    return parent;
  }

  public Image getImage() {
    switch (getStatus()) {
    case STATUS_OK:
      return IMAGE_OK;
    case STATUS_MISSING:
      return IMAGE_MISSING;
    case STATUS_PARTIAL:
      return IMAGE_PARTIAL;
    default:
      return IMAGE_UNKNOWN;
    }
  }

  public synchronized boolean isDirty() {
    for (Object child : getChildren()) {
      if (child instanceof AbstractModelObject) {
        AbstractModelObject c = (AbstractModelObject) child;
        if (c.isDirty())
          return true;
      }
    }
    return false;
  }

  public synchronized void refreshUI() {
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        firePropertyChange("status", null, getStatus());
        firePropertyChange("code", null, getCode());
        if (getParent() != null) {
          getParent().firePropertyChange("children", null, getParent().getChildren());
        }
        firePropertyChange("children", null, getChildren());
      }
    });
  }

  public static Comparator<? super AbstractModelObject> getComparator() {
    if (modelComparator == null) {
      modelComparator = new Comparator<AbstractModelObject>() {
        @Override
        public int compare(AbstractModelObject o1, AbstractModelObject o2) {
          // Always bump module information at the top
          if (o1 instanceof ModuleInformation)
            return -1;
          if (o2 instanceof ModuleInformation)
            return 1;

          // Alphabetical ordering on names
          return o1.getName().compareTo(o2.getName());
        }
      };
    }
    return modelComparator;
  }
}
