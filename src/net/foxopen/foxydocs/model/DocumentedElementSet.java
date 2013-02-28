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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.foxopen.foxydocs.model.abstractObject.AbstractDocumentedElement;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

public class DocumentedElementSet extends AbstractModelObject implements AbstractDocumentedElement {

  private String type;
  private final List<AbstractModelObject> documentedElements = Collections.synchronizedList(new ArrayList<AbstractModelObject>());

  public DocumentedElementSet(String type, AbstractModelObject parent) {
    super(parent);
    setType(type);
  }

  public List<AbstractModelObject> getDocumentationEntries() {
    return documentedElements;
  }

  public void add(DocumentedElement e) {
    documentedElements.add(e);
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public int size() {
    return documentedElements.size();
  }

  @Override
  public String getName() {
    return getType() + " " + (isDirty() ? "*" : "");
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    return getDocumentationEntries();
  }

  @Override
  public String getDocumentation() {
    String buffer = "";
    for (AbstractModelObject doc : getChildren()) {
      buffer += "- " + doc.getName() + "\n";
      buffer += doc.getDocumentation() + "\n\n";
    }
    return buffer;
  }

  @Override
  public String getCode() {
    return getParent().getCode();
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getComments() {
    return "";
  }

  @Override
  public String getPrecondition() {
    return "";
  }
}
