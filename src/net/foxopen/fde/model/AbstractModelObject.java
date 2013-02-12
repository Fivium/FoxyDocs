package net.foxopen.fde.model;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Observable;

public abstract class AbstractModelObject extends Observable {

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  abstract public List<AbstractModelObject> getChildren();

  abstract public String getName();

  public boolean getStatus() {
    for (AbstractModelObject child : getChildren()) {
      if (!child.getStatus())
        return false;
    }
    return true;
  }

  public boolean hasChildren() {
    return getChildren().size() > 0;
  }

  public String getDocumentation() {
    return null;
  }

  public String getCode() {
    return null;
  }

  public void setDocumentation(String documentation) {
    // TODO Auto-generated method stub
  }

  public void setCode(String code) {
    // TODO Auto-generated method stub
  }

}
