package net.foxopen.fde.model;

import java.util.List;
import static net.foxopen.utils.Constants.*;

import net.foxopen.fde.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;
import org.jdom2.located.Located;

public class DocumentationEntry extends AbstractModelObject {
  private String documentation = "";
  private String oldDocumentation = "";
  private String name;
  private int lineNumber = -1;

  public DocumentationEntry(Element node, AbstractModelObject parent) {
    if (node == null)
      throw new IllegalArgumentException("The node cannot be null");
    
    // Extract the line number. Must use SAX and a located element
    if(node instanceof Located){
      Located locatedNode = (Located) node;
      lineNumber = locatedNode.getLine();
    }

    this.parent = parent;

    Element docNode = node.getChild("documentation", NAMESPACE_FM);
    if (docNode != null) {
      firePropertyChange("docContent", this.documentation, this.documentation = docNode.getChild("description", NAMESPACE_FM).getTextNormalize());
      oldDocumentation = documentation;
    }

    String name = node.getAttributeValue("name");
    if (name == null) {
      setName(node.getName());
    } else {
      setName(name);
    }
  }
  
  public int getLineNumber(){
    return lineNumber;
  }

  public int getStatus() {
    return documentation.trim().length() > 0 ? STATUS_OK : STATUS_MISSING;
  }

  public String getCode() {
    return getParent().getCode();
  }

  public String getName() {
    return name + " " + (isDirty() ? "*" : "");
  }

  public boolean isDirty() {
    return !documentation.equals(oldDocumentation);
  }

  @Override
  public void setDocumentation(String content) {
    int oldStatus = getStatus();
    firePropertyChange("docContent", this.documentation, this.documentation = content);
    firePropertyChange("dirty", oldDocumentation, isDirty());
    firePropertyChange("status", oldStatus, getStatus());
  }

  public void setName(String name) {
    if (name == null || name.trim() == "")
      throw new IllegalArgumentException("Entry name is null");
    firePropertyChange("name", this.name, this.name = name);
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
