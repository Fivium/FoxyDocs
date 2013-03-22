/*
Copyright (c) 2013, ENERGY DEVELOPMENT UNIT (INFORMATION TECHNOLOGY)
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
 * Neither the name of the DEPARTMENT OF ENERGY AND CLIMATE CHANGE nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package net.foxopen.foxydocs.model;

import static net.foxopen.foxydocs.FoxyDocs.NAMESPACE_FM;

import java.util.ArrayList;
import java.util.List;

import net.foxopen.foxydocs.FoxyDocs;
import net.foxopen.foxydocs.model.abstractObject.AbstractDocumentedElement;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;
import org.jdom2.located.Located;

public class DocumentedElement extends AbstractDocumentedElement {
  private String previousDocumentationHash;
  private String name;
  private Element elementNode;

  public DocumentedElement(Element node, AbstractModelObject parent) {
    super(parent, node.getChild("documentation", NAMESPACE_FM), FoxyDocs.ELEMENT_FIELDS);
    this.elementNode = node;

    // Get an existing documentation node. If it does not exist, create it
    if (getNode() == null) {
      setNode(new Element("documentation", NAMESPACE_FM));
    }

    previousDocumentationHash = getHash();

    String nodeName = node.getAttributeValue("name");
    if (nodeName == null) {
      setName(node.getName());
    } else {
      setName(nodeName);
    }

  }

  public int getLineNumber() {
    // Extract the line number. Must use SAX and a located element
    if (elementNode instanceof Located) {
      Located locatedNode = (Located) elementNode;
      return locatedNode.getLine();
    }
    return 0;
  }

  @Override
  public synchronized boolean isDirty() {
    return !getHash().equals(previousDocumentationHash);
  }

  @Override
  public void save() throws Exception {
    if (!isDirty())
      return;

    if (elementNode.getChild("documentation", NAMESPACE_FM) == null) {
      this.elementNode.addContent(getNode());
    }

    // New become old
    previousDocumentationHash = getHash();
    firePropertyChange("dirty", true, isDirty());
  }

  public void setName(String name) {
    if (name == null || name.trim() == "")
      throw new IllegalArgumentException("Entry name is null");
    firePropertyChange("name", this.name, this.name = name);
  }

  @Override
  public synchronized boolean getHasChildren() {
    return false;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getFoxModule().getName() + "::" + name;
  }

  public List<String> getActionCalls() {
    List<String> actionCalls = new ArrayList<>();
    // Action calls
    List<Element> aCalls = runXpath("//fm:call", elementNode);
    for (Element e : aCalls) {
      actionCalls.add(e.getAttributeValue("action"));
    }

    return actionCalls;
  }

  public List<DocumentedElement> getActions() {
    List<DocumentedElement> actions = new ArrayList<>();
    if (elementNode.getName().equals("action")) {
      actions.add(this);
      return actions;
    }

    for (DocumentedElement e : getChildren(DocumentedElement.class)) {
      actions.addAll(e.getActions());
    }

    return actions;
  }

}
