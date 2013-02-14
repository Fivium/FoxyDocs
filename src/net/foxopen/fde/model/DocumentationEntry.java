package net.foxopen.fde.model;

import java.util.List;
import static net.foxopen.utils.Constants.NAMESPACE_FM;

import net.foxopen.fde.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;

public class DocumentationEntry extends AbstractModelObject {
  private String documentation = "";
  private String name;
  private boolean dirty = false;

  public DocumentationEntry(Element node, AbstractModelObject parent) {
    if (node == null)
      throw new IllegalArgumentException("The node cannot be null");

    this.parent = parent;

    Element docNode = node.getChild("documentation", NAMESPACE_FM);
    if (docNode != null) {
      firePropertyChange("docContent", this.documentation, this.documentation = docNode.getChild("description", NAMESPACE_FM).getTextNormalize());
    }
   
    String name = node.getAttributeValue("name");
    if (name == null) {
      setName(node.getName());
    } else {
      setName(name);
    }
  }

  public boolean getStatus() {
    return documentation.trim().length() > 0;
  }

  public String getCode() {
    return getParent().getCode();
  }

  public String getName() {
    return name + " " + (isDirty() ? "*" : "");
  }

  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void setDocumentation(String content) {
    boolean oldStatus = getStatus();
    firePropertyChange("docContent", this.documentation, this.documentation = content);
    setDirty(true);
    firePropertyChange("status", oldStatus, getStatus());
  }

  public void setName(String name) {
    if (name == null || name.trim() == "")
      throw new IllegalArgumentException("Entry name is null");
    firePropertyChange("name", this.name, this.name = name);
  }

  public void setDirty(boolean dirty) {
    firePropertyChange("dirty", this.dirty, this.dirty = dirty);
  }

  @Override
  public String getDocumentation() {
    return documentation;
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    // No children at this point
    return null;
  }

  @Override
  public boolean getHasChildren() {
    return false;
  }
}
