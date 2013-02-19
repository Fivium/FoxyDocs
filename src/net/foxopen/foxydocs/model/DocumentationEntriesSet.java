package net.foxopen.foxydocs.model;

import java.util.ArrayList;
import java.util.List;

import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

public class DocumentationEntriesSet extends AbstractModelObject {

  private String type;
  private final ArrayList<AbstractModelObject> documentationEntries = new ArrayList<AbstractModelObject>();

  public DocumentationEntriesSet(String type, AbstractModelObject parent) {
    setType(type);
    this.parent = parent;
  }

  public List<AbstractModelObject> getDocumentationEntries() {
    return documentationEntries;
  }

  public void add(DocumentationEntry e) {
    documentationEntries.add(e);
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public int size() {
    return documentationEntries.size();
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
  public String getCode() {
    return getParent().getCode();
  }
}
