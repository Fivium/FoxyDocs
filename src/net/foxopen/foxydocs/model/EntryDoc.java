package net.foxopen.foxydocs.model;

import static net.foxopen.foxydocs.FoxyDocs.NAMESPACE_FM;

import org.jdom2.Element;

import net.foxopen.foxydocs.FoxyDocs;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

public class EntryDoc extends AbstractModelObject {

  private Element node;
  private String attr;
  private boolean mandatory = false;

  public EntryDoc(AbstractModelObject parent, Element parentNode, String attr) {
    super(parent);

    if (attr.startsWith("*")) {
      mandatory = true;
      attr = attr.substring(1);
    }

    this.attr = attr;
    this.node = parentNode.getChild(attr, NAMESPACE_FM);
    if (this.node == null) {
      this.node = new Element(attr, NAMESPACE_FM);
      parentNode.addContent(node);
    }
  }

  @Override
  public synchronized int getStatus() {
    if (!mandatory)
      return FoxyDocs.STATUS_UNKNOWN;
    if (node == null || node.getTextTrim().length() == 0)
      return FoxyDocs.STATUS_MISSING;
    return node.getContentSize() > 0 ? FoxyDocs.STATUS_OK : FoxyDocs.STATUS_MISSING;
  }

  @Override
  public String getName() {
    return (mandatory ? "* " : "") + attr.substring(0, 1).toUpperCase() + attr.substring(1);
  }

  public void setValue(String text) {
    node.removeContent();
    if (text != null && text.trim().length() > 0)
      node.addContent(text);

    firePropertyChange("dirty", false, isDirty());
    firePropertyChange("status", FoxyDocs.STATUS_UNKNOWN, getStatus());
  }

  public String getKey() {
    return attr;
  }

  public String getValue() {
    if (node == null)
      return "";
    return node.getTextNormalize();
  }

  public static String getDisplayedName(String key) {
    StringBuffer buffer = new StringBuffer();
    if (key.startsWith("*")) {
      buffer.append("* ");
      key = key.substring(1);
    }
    buffer.append(key.substring(0, 1).toUpperCase());
    buffer.append(key.substring(1));
    return buffer.toString();
  }

}
