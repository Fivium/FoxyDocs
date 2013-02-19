package net.foxopen.fde.model;

import java.util.List;
import static net.foxopen.utils.FoxyDocs.*;

import net.foxopen.fde.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;
import org.jdom2.located.Located;

public class DocumentationEntry extends AbstractModelObject {
  private String documentation = "";
  private String oldDocumentation = "";
  private String name;
  private int lineNumber = -1;
  private final Element node;

  public DocumentationEntry(Element node, AbstractModelObject parent) {
    if (node == null)
      throw new IllegalArgumentException("The node cannot be null");

    this.node = node;

    // Extract the line number. Must use SAX and a located element
    if (node instanceof Located) {
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

  public int getLineNumber() {
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

  @Override
  public void save() {
    if(!isDirty())
      return;
    // Update or create the documentation node
    Element documentationNode = node.getChild("documentation", NAMESPACE_FM);
    if (documentationNode == null) {
      documentationNode = getDocumentationStructure();
      node.addContent(documentationNode);
    }
    documentationNode.getChild("description", NAMESPACE_FM).addContent(this.documentation);
    
    // New become old
    oldDocumentation = getDocumentation();
    firePropertyChange("dirty", oldDocumentation, isDirty());
    firePropertyChange("status", -1, getStatus());
  }

  public Element getDocumentationStructure() {
    Element description = new Element("description", NAMESPACE_FM);
    Element documentation = new Element("documentation", NAMESPACE_FM);
    documentation.addContent(description);
    return documentation;
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
