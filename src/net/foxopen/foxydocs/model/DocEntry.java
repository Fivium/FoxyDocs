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
import static net.foxopen.foxydocs.FoxyDocs.STATUS_MISSING;
import static net.foxopen.foxydocs.FoxyDocs.STATUS_OK;
import static net.foxopen.foxydocs.FoxyDocs.STATUS_UNKNOWN;

import java.util.HashMap;
import java.util.List;

import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;

public class DocEntry extends AbstractModelObject {

  protected final Element node;
  protected Element docNode;

  private final HashMap<String, Element> attributes = new HashMap<String, Element>();
  private final static String[] attributeList = new String[] { "comments", "description", "pre-condition" };

  public DocEntry(Element node, AbstractModelObject parent) {
    super(parent);

    this.node = node;

    // Get an existing documentation node. If it does not exist, create it
    docNode = node.getChild("documentation", NAMESPACE_FM);
    if (docNode == null) {
      // Add a new empty node
      docNode = new Element("documentation", NAMESPACE_FM);
      node.addContent(docNode);
    }

    for (String attr : attributeList) {
      attributes.put(attr, docNode.getChild(attr, NAMESPACE_FM));
    }
  }

  private Element getAttributeNode(String key) {
    return attributes.get(key);
  }

  private String getAttributeText(String key) {
    Element tNode = getAttributeNode(key);
    if (tNode == null || tNode.getContent() == null) {
      return "";
    }
    return tNode.getTextNormalize();
  }

  private void setAttributeText(String key, String content) {
    Element tNode = getAttributeNode(key);
    // If there is no node...
    if (tNode == null) {
      tNode = new Element(key, NAMESPACE_FM);
      docNode.addContent(tNode);
    } else {
      tNode.removeContent();
    }
    if (content != null && content.trim().length() > 0)
      tNode.addContent(content);
    firePropertyChange("status", STATUS_UNKNOWN, getStatus());
    firePropertyChange("dirty", null, isDirty());
  }

  @Override
  public String toString() {
    return getDescription() + getComments() + getPrecondition();
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  public int getStatus(String key) {
    Element tNode = getAttributeNode(key);
    if (tNode == null) {
      return STATUS_MISSING;
    }
    return tNode.getContentSize() == 0 ? STATUS_MISSING : STATUS_OK;
  }

  @Override
  public synchronized int getStatus() {
    // Precondition is not mandatory
    return getStatus("comments") | getStatus("description");
  }

  public String[] getAttributeKeys() {
    return attributeList;
  }

  public String getDescription() {
    return getAttributeText("description");
  }

  public void setDescription(String content) {
    setAttributeText("description", content);
  }

  public String getComments() {
    return getAttributeText("comments");
  }

  public void setComments(String content) {
    setAttributeText("comments", content);
  }

  public String getPrecondition() {
    return getAttributeText("pre-condition");
  }

  public void setPrecondition(String content) {
    setAttributeText("pre-condition", content);
  }
}
