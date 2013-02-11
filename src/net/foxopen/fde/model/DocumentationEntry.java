package net.foxopen.fde.model;

import static net.foxopen.utils.Logger.logStdout;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;

public class DocumentationEntry extends AbstractModelObject {
  // TODO make generic
  public static Namespace ns_fm = Namespace.getNamespace("fm", "http://www.og.dti.gov/fox_module");
  private static XMLOutputter serializer = new XMLOutputter();

  private String status;
  private String content;
  private String code;
  private String name;

  public DocumentationEntry(Element node) {
    if (node == null)
      throw new IllegalArgumentException("The node cannob be null");

    Element docNode = node.getChild("documentation", ns_fm);
    if (docNode == null) {
      setStatus("missing");
    } else {
      setStatus("ok");
      setContent(docNode.getChild("description", ns_fm).getTextNormalize());
    }
    setCode(serializer.outputString(node));

    String name = node.getAttributeValue("name");
    if (name == null) {
      setName(node.getName());
    } else {
      setName(name);
    }

    logStdout(toString());
  }

  public String getStatus() {
    return status;
  }

  public String getContent() {
    return content;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public void setStatus(String status) {
    firePropertyChange("status", this.status, this.status = status);
  }

  public void setContent(String content) {
    if (content == null || content.trim() == "")
      throw new IllegalArgumentException("Documentation content cannot be empty if assigned");
    firePropertyChange("docContent", this.content, this.content = content);
  }

  public void setCode(String code) {
    if (code == null || code.trim() == "")
      throw new IllegalArgumentException("Code cannot be empty");
    firePropertyChange("code", this.code, this.code = code);
  }

  public void setName(String name) {
    if (name == null || name.trim() == "")
      throw new IllegalArgumentException("Entry name is null");
    firePropertyChange("name", this.name, this.name = name);
  }

  public String toString() {
    return name + " : " + status;
  }

  @Override
  public String getDocumentation() {
    return content;
  }

  @Override
  public void setDocumentation(String documentation) {
    setContent(documentation);
  }
}
