package net.foxopen.fde.model.tree;

import java.awt.Image;
import java.util.List;

import net.foxopen.fde.model.AbstractModelObject;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider implements ITreeContentProvider {

  public static interface ITreeNode {
    public String getName();

    public Image getImage();

    public List<AbstractModelObject> getChildren();

    public boolean hasChildren();

    public ITreeNode getParent();
    
    public String getDocumentation();
    
    public String getCode();
    
    public void setDocumentation(String documentation);
    
    public void setCode(String code);
  }

  public Object[] getChildren(Object parentElement) {
    return ((ITreeNode) parentElement).getChildren().toArray();
  }

  public Object getParent(Object element) {
    return ((ITreeNode) element).getParent();
  }

  public boolean hasChildren(Object element) {
    return ((ITreeNode) element).hasChildren();
  }

  public Object[] getElements(Object inputElement) {
    return getChildren(inputElement);
  }

  public void dispose() {
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // TODO Auto-generated method stub
    
  }
}