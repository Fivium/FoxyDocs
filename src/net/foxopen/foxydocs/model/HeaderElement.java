package net.foxopen.foxydocs.model;

import java.util.HashMap;

import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;
import static net.foxopen.FoxyDocs.NAMESPACE_FM;

import org.jdom2.Element;

public class HeaderElement extends DocumentedElement {

  private final HashMap<String, Element> attributes = new HashMap<String, Element>();
  private final static String[] attributeList = new String[] { "name", "title", "application-title", "version-no", "version-desc", "history", "description", "build-notes", "help-text", };

  public HeaderElement(Element node, AbstractModelObject parent) {
    super(node, parent);
    for (String attr : attributeList) {
      Element attributeElement = node.getChild(attr, NAMESPACE_FM);
      if (attributeElement == null) {
        attributeElement = new Element(attr, NAMESPACE_FM);
        node.addContent(attributeElement);
      }
      attributes.put(attr, attributeElement);
    }
  }
  
}
