package net.foxopen.foxydocs.model;

import static net.foxopen.foxydocs.FoxyDocs.NAMESPACE_FM;

import org.jdom2.Element;

public class DocEntry {

  private final Element node;

  private Element comments;
  private Element description;
  private Element precondition;

  public DocEntry(Element node) {
    this.node = node;
    comments = node.getChild("comments", NAMESPACE_FM);
    description = node.getChild("description", NAMESPACE_FM);
    precondition = node.getChild("pre-condition", NAMESPACE_FM);
  }

  public boolean isEmpty() {
    return description.getTextTrim().length() == 0;
  }

  public static Element getDocumentationStructure() {
    // Root
    Element documentation = new Element("documentation", NAMESPACE_FM);
    // Children
    Element comments = new Element("comments", NAMESPACE_FM);
    documentation.addContent(comments);
    Element description = new Element("description", NAMESPACE_FM);
    documentation.addContent(description);
    Element precondition = new Element("pre-condition", NAMESPACE_FM);
    documentation.addContent(precondition);
    return documentation;
  }

  public DocEntry setDocumentation(String content) {
    description.removeContent();
    description.addContent(content);
    return this;
  }

  @Override
  public String toString() {
    if (description == null) {
      return "";
    }
    return description.getTextNormalize();
  }

}
