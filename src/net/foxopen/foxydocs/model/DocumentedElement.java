package net.foxopen.foxydocs.model;

import java.util.List;

import static net.foxopen.foxydocs.FoxyDocs.*;

import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;
import org.jdom2.located.Located;

public class DocumentedElement extends AbstractModelObject {
  private DocEntry documentation;
  private String previousDocumentationContent;
  private String name;
  private int lineNumber = -1;

  public DocumentedElement(Element node, AbstractModelObject parent) {
    super(parent);
    if (node == null)
      throw new IllegalArgumentException("The node cannot be null");

    // Extract the line number. Must use SAX and a located element
    if (node instanceof Located) {
      Located locatedNode = (Located) node;
      lineNumber = locatedNode.getLine();
    }

    // Get an existing documentation node. If it does not exist, create it
    Element docNode = node.getChild("documentation", NAMESPACE_FM);
    if (docNode == null) {
      // Add a new empty node
      docNode = DocEntry.getDocumentationStructure();
      node.addContent(docNode);
    }

    // Create documentation
    DocEntry newDoc = new DocEntry(docNode);
    documentation = newDoc;
    previousDocumentationContent = newDoc.toString();

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
    return documentation.isEmpty() ? STATUS_MISSING : STATUS_OK;
  }

  public String getCode() {
    return getParent().getCode();
  }

  public String getName() {
    return name + " " + (isDirty() ? "*" : "");
  }

  public boolean isDirty() {
    return !documentation.toString().equals(previousDocumentationContent);
  }

  @Override
  public void setDocumentation(String content) {
    int oldStatus = getStatus();
    boolean wasDirty = isDirty();
    firePropertyChange("documentation", documentation, documentation.setDocumentation(content));
    firePropertyChange("dirty", wasDirty, isDirty());
    firePropertyChange("status", oldStatus, getStatus());
  }

  @Override
  public void save() throws Exception {
    if (!isDirty())
      return;
  
    // New become old
    previousDocumentationContent = documentation.toString();
    firePropertyChange("dirty", true, isDirty());
    firePropertyChange("status", -1, getStatus());
  }

  public void setName(String name) {
    if (name == null || name.trim() == "")
      throw new IllegalArgumentException("Entry name is null");
    firePropertyChange("name", this.name, this.name = name);
  }

  @Override
  public String getDocumentation() {
    return documentation.toString();
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
