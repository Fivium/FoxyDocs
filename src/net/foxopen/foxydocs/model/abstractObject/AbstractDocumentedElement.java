package net.foxopen.foxydocs.model.abstractObject;

import static net.foxopen.foxydocs.FoxyDocs.STATUS_UNKNOWN;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import net.foxopen.foxydocs.model.EntryDoc;

import org.jdom2.Element;

public abstract class AbstractDocumentedElement extends AbstractModelObject implements InvocationHandler {

  protected final ArrayList<EntryDoc> elements = new ArrayList<>();
  protected final String[] attributes;

  private Element node;

  protected AbstractDocumentedElement(AbstractModelObject parent, Element node, String... attributes) {
    super(parent);
    this.attributes = attributes;
    setNode(node);
  }

  protected void setNode(Element node) {
    if (node != null) {
      this.node = node;
      for (String attr : attributes) {
        elements.add(new EntryDoc(this, node, attr));
      }
    }
  }

  protected Element getNode() {
    return node;
  }

  public String getHash() {
    StringBuilder hash = new StringBuilder();
    for (EntryDoc e : elements) {
      hash.append(e.getValue().hashCode());
    }
    return hash.toString();
  }

  @Override
  public synchronized int getStatus() {
    int out = STATUS_UNKNOWN;
    for (EntryDoc e : elements) {
      out |= e.getStatus();
    }
    return out;
  }

  public Collection<EntryDoc> getElements() {
    return elements;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    throw new RuntimeException(methodName);
  }

}