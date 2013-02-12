package net.foxopen.fde.model;

import java.util.ArrayList;
import java.util.List;

public class DocumentationEntriesSet extends AbstractModelObject {

  private String type;
  private final ArrayList<AbstractModelObject> documentationEntries = new ArrayList<AbstractModelObject>();

  public DocumentationEntriesSet(String type) {
    setType(type);
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
    return getType()+" "+(isDirty()?"*":"");
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    return getDocumentationEntries();
  }

  public boolean isDirty() {
    for (AbstractModelObject a : documentationEntries) {
      if (((DocumentationEntry) a).isDirty())
        return true;
    }
    return false;
  }

  @Override
  public String getDocumentation() {
    String buffer = "";
    for (AbstractModelObject doc : getChildren()) {
      buffer += doc.getDocumentation() + "\n";
    }
    return buffer;
  }

  @Override
  public String getCode() {
    String buffer = "";
    for (AbstractModelObject doc : getChildren()) {
      buffer += doc.getCode() + "\n";
    }
    return buffer;
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
