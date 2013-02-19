package net.foxopen.fde.model.abstractObject;

import static net.foxopen.utils.Constants.IMAGE_MISSING;
import static net.foxopen.utils.Constants.IMAGE_OK;
import static net.foxopen.utils.Constants.IMAGE_PARTIAL;
import static net.foxopen.utils.Constants.IMAGE_UNKNOWN;
import static net.foxopen.utils.Constants.STATUS_MISSING;
import static net.foxopen.utils.Constants.STATUS_OK;
import static net.foxopen.utils.Constants.STATUS_PARTIAL;
import static net.foxopen.utils.Constants.STATUS_UNKNOWN;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Observable;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractModelObject extends Observable {

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  protected AbstractModelObject parent;
  protected Image image;

  abstract public List<AbstractModelObject> getChildren();

  abstract public String getName();

  public void addChild(AbstractModelObject child) {
    getChildren().add(child);
  }

  protected AbstractModelObject() {

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
        firePropertyChange("name", null, getName());
        if (getParent() != null) {
          // Cascade propagate the change
          getParent().firePropertyChange("status", evt.getOldValue(), evt.getNewValue());
        }
      }
    });
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  synchronized public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  public int getStatus() {
    int status = STATUS_UNKNOWN;
    for (Object child : getChildren()) {
      if (child instanceof AbstractModelObject) {
        AbstractModelObject c = (AbstractModelObject) child;
        status |= c.getStatus();
      }
    }
    return status;
  }

  public void clear() {
    if (getHasChildren())
      getChildren().clear();
    firePropertyChange("children", null, getChildren());
  }

  public boolean getHasChildren() {
    return getChildren() != null && getChildren().size() > 0;
  }

  public String getDocumentation() {
    throw new IllegalArgumentException("You cannot call this method directly. Please override it.");
  }

  public String getCode() {
    throw new IllegalArgumentException("You cannot call this method directly. Please override it.");
  }

  public void setDocumentation(String documentation) {
    throw new IllegalArgumentException("You cannot call this method directly. Please override it.");
  }

  public AbstractModelObject getParent() {
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

  public boolean isDirty() {
    for (Object child : getChildren()) {
      if (child instanceof AbstractModelObject) {
        AbstractModelObject c = (AbstractModelObject) child;
        if (c.isDirty())
          return true;
      }
    }
    return false;
  }

  public void refreshUI() {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        firePropertyChange("status", null, getStatus());
        if (getParent() != null) {
          getParent().firePropertyChange("children", null, getParent().getChildren());
        }
        firePropertyChange("children", null, getChildren());
      }
    });
  }
}
