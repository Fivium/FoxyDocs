package net.foxopen.fde.model.abstractObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Observable;

import static net.foxopen.utils.Constants.*;

import org.eclipse.swt.graphics.Image;

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

  public void cascadeFirePropertyChange(String propertyName, Object oldValue, Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    if (getParent() != null) {
      getParent().cascadeFirePropertyChange(propertyName, oldValue, newValue);
    }
  }

  protected AbstractModelObject parent;

  abstract public List<? extends AbstractModelObject> getChildren();

  abstract public String getName();

  public boolean getStatus() {
    for (AbstractModelObject child : getChildren()) {
      if (!child.getStatus())
        return false;
    }
    return true;
  }

  public boolean getHasChildren() {
    return getChildren().size() > 0;
  }

  public String getDocumentation() {
    return null;
  }

  public String getCode() {
    return null;
  }

  public AbstractModelObject getParent() {
    return parent;
  }

  public Image getImage() {
    return getStatus() ? IMAGE_OK : IMAGE_MISSING;
  }

  public boolean isDirty() {
    for (AbstractModelObject a : getChildren()) {
      if (a.isDirty())
        return true;
    }
    return false;
  }

  public void setDocumentation(String documentation) {
    // TODO Auto-generated method stub
  }

  public void setCode(String code) {
    // TODO Auto-generated method stub
  }

}
