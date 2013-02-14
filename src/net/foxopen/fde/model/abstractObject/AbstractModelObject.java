package net.foxopen.fde.model.abstractObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Observable;

import static net.foxopen.utils.Constants.*;

import org.eclipse.swt.graphics.Image;

public abstract class AbstractModelObject extends Observable {

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  protected AbstractModelObject parent;
  protected Image image;

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

  abstract public List<? extends AbstractModelObject> getChildren();

  abstract public String getName();

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
    for (AbstractModelObject child : getChildren()) {
      status |= child.getStatus(); 
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
    for (AbstractModelObject a : getChildren()) {
      if (a.isDirty())
        return true;
    }
    return false;
  }

}
