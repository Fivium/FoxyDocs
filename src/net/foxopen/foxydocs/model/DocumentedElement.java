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

import net.foxopen.foxydocs.model.abstractObject.AbstractDocumentedElement;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;
import org.jdom2.located.Located;

public class DocumentedElement extends AbstractDocumentedElement {
  private String previousDocumentationHash;
  private String name;
  private final DocEntry documentationEntry;
  private final Element node;
  private final boolean isHeader;
  
  public  DocumentedElement(Element node, AbstractModelObject parent, boolean isHeader){
    super(parent);
    this.node = node;
    this.isHeader = isHeader;

    documentationEntry = new DocEntry(node, this);
    previousDocumentationHash = documentationEntry.getHash();

    String nodeName = node.getAttributeValue("name");
    if (nodeName == null) {
      setName(node.getName());
    } else {
      setName(nodeName);
    }
  }
  public DocumentedElement(Element node, AbstractModelObject parent) {
    this(node, parent, false);
  }
  
  public boolean isHeader(){
    return isHeader;
  }

  public int getLineNumber() {
    // Extract the line number. Must use SAX and a located element
    if (node instanceof Located) {
      Located locatedNode = (Located) node;
      return locatedNode.getLine();
    }
    return 0;
  }

  @Override
  public String getCode() {
    return getParent().getCode();
  }

  @Override
  public synchronized boolean isDirty() {
    return !documentationEntry.getHash().equals(previousDocumentationHash);
  }

  @Override
  public void save() throws Exception {
    if (!isDirty())
      return;
    
    documentationEntry.save();

    // New become old
    previousDocumentationHash = documentationEntry.getHash();
    firePropertyChange("dirty", true, isDirty());
  }

  public void setName(String name) {
    if (name == null || name.trim() == "")
      throw new IllegalArgumentException("Entry name is null");
    firePropertyChange("displayedName", this.name, this.name = name);
  }

  @Override
  public synchronized boolean getHasChildren() {
    return false;
  }

  @Override
  public synchronized int getStatus() {
    return documentationEntry.getStatus();
  }

  /* (non-Javadoc)
   * @see net.foxopen.foxydocs.model.AbstractDocumentedElement#getDescription()
   */
  @Override
  public String getDescription() {
    return documentationEntry.getDescription();
  }

  /* (non-Javadoc)
   * @see net.foxopen.foxydocs.model.AbstractDocumentedElement#getComments()
   */
  @Override
  public String getComments() {
    return documentationEntry.getComments();
  }

  /* (non-Javadoc)
   * @see net.foxopen.foxydocs.model.AbstractDocumentedElement#getPrecondition()
   */
  @Override
  public String getPrecondition() {
    return documentationEntry.getPrecondition();
  }
  
  @Override
  public void setDescription(String content) {
    documentationEntry.setDescription(content);
  }
  
  @Override
  public void setComments(String content) {
    documentationEntry.setComments(content);
  }
  
  @Override
  public void setPrecondition(String content) {
    documentationEntry.setPrecondition(content);
  }
  
  @Override
  public String getName() {
    return name;
  }

}
