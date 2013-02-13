package net.foxopen.fde.model;

import java.util.List;

import net.foxopen.fde.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class DocumentationEntry extends AbstractModelObject {
  // TODO make generic
  public static Namespace ns_fm = Namespace.getNamespace("fm", "http://www.og.dti.gov/fox_module");

  private String documentation = "";
  private String name;
  private boolean dirty = false;

  public DocumentationEntry(Element node, AbstractModelObject parent) {
    if (node == null)
      throw new IllegalArgumentException("The node cannot be null");
    
    this.parent = parent;

    Element docNode = node.getChild("documentation", ns_fm);
    if (docNode == null) {
      setStatus(false);
    } else {
      setStatus(true);
      documentation = docNode.getChild("description", ns_fm).getTextNormalize();
    }
    
    String name = node.getAttributeValue("name");
    if (name == null) {
      setName(node.getName());
    } else {
      setName(name);
    }
  }

  public boolean getStatus() {
    return documentation.trim().length()>0;
  }

  public String getCode() {
    return getParent().getCode();
    // TODO Hightlight the code
  }

  public String getName() {
    return name + " " + (isDirty() ? "*" : "");
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setStatus(boolean status) {
    firePropertyChange("status", false, getStatus());
  }

  @Override
  public void setDocumentation(String content) {
    if (content == null || content.trim() == "")
      throw new IllegalArgumentException("Documentation content cannot be empty if assigned");
    firePropertyChange("docContent", this.documentation, this.documentation = content);
    setDirty(true);
    cascadeFirePropertyChange("status", null, getStatus());
  }

  public void setName(String name) {
    if (name == null || name.trim() == "")
      throw new IllegalArgumentException("Entry name is null");
    firePropertyChange("name", this.name, this.name = name);
  }

  public void setDirty(boolean dirty) {
    cascadeFirePropertyChange("dirty", this.dirty, this.dirty = dirty);
    cascadeFirePropertyChange("name", this.name, getName());
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
