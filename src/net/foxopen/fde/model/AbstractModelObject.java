package net.foxopen.fde.model;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Observable;

import net.foxopen.fde.model.tree.TreeContentProvider.ITreeNode;

public abstract class AbstractModelObject extends Observable implements ITreeNode {
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

  @Override
  public Image getImage() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public List<AbstractModelObject> getChildren(){
    throw new IllegalArgumentException("Shouldn't be there");
  }
  
  public String getName(){
    throw new IllegalArgumentException("Shouldn't be there");
  }
  
  public boolean getStatus() {
    for(AbstractModelObject child :getChildren()){
      if(!child.getStatus())
        return false;
    }
    return true;
  }

  @Override
  public boolean hasChildren() {
    return getChildren().size()>0;
  }

  @Override
  public ITreeNode getParent() {
    return null;
  }

  @Override
  public String getDocumentation() {
    return null;
  }

  @Override
  public String getCode() {
    return null;
  }

  @Override
  public void setDocumentation(String documentation) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setCode(String code) {
    // TODO Auto-generated method stub
  }

}
