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

import java.util.List;

import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;

public class DocEntry extends AbstractModelObject {

  private Element comments;
  private Element description;
  private Element precondition;

  public DocEntry(Element node, AbstractModelObject parent) {
    super(parent);

    // Get an existing documentation node. If it does not exist, create it
    Element docNode = node.getChild("documentation", NAMESPACE_FM);
    if (docNode == null) {
      // Add a new empty node
      docNode = DocEntry.getDocumentationStructure();
      node.addContent(docNode);
    }

    comments = docNode.getChild("comments", NAMESPACE_FM);
    description = docNode.getChild("description", NAMESPACE_FM);
    precondition = docNode.getChild("pre-condition", NAMESPACE_FM);
  }

  public boolean isEmpty() {
    return description == null || description.getTextTrim().length() == 0;
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

  public String getDescription() {
    if (description == null)
      return "";
    return description.getTextNormalize();
  }

  public void setDescription(String content) {
    updateNode(description, content);
  }

  public String getComments() {
    if (comments == null)
      return "";
    return comments.getTextNormalize();
  }

  public void setComments(String content) {
    updateNode(comments, content);
  }

  public String getPrecondition() {
    if (precondition == null)
      return "";
    return precondition.getTextNormalize();
  }

  public void setPrecondition(String content) {
    updateNode(precondition, content);
  }

  private void updateNode(Element node, String content) {
    node.removeContent();
    if (content != null && content.trim().length() > 0)
      node.addContent(content);
    firePropertyChange("status", -1, getStatus());
    firePropertyChange("dirty", null, isDirty());
  }

  @Override
  public String toString() {
    return getDescription() + getComments() + getPrecondition();
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getStatus(Element node) {
    return node.getContentSize() == 0 ? STATUS_MISSING : STATUS_OK;
  }

  @Override
  public int getStatus() {
    // Precondition is not mandatory
    return getStatus(comments) | getStatus(description);
  }
}
