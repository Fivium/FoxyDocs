package net.foxopen.foxydocs.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

public class DocumentedElementSet extends AbstractModelObject {

  private String type;
  private final ArrayList<AbstractModelObject> documentedElements = new ArrayList<AbstractModelObject>();

  public DocumentedElementSet(String type, AbstractModelObject parent) {
    super(parent);
    setType(type);
  }

  public List<AbstractModelObject> getDocumentationEntries() {
    return documentedElements;
  }

  public void add(DocumentedElement e) {
    documentedElements.add(e);
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public int size() {
    return documentedElements.size();
  }

  @Override
  public String getName() {
    return getType() + " " + (isDirty() ? "*" : "");
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    return getDocumentationEntries();
  }

  @Override
  public String getDocumentation() {
    String buffer = "";
    for (AbstractModelObject doc : getChildren()) {
      buffer += "- " + doc.getName() + "\n";
      buffer += doc.getDocumentation() + "\n\n";
    }
    return buffer;
  }

  @Override
  public String getCode() throws IOException {
    return getParent().getCode();
  }
}
