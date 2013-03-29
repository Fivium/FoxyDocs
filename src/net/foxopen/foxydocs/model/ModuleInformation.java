/*
Copyright (c) 2013, Fivium Ltd.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of Fivium Ltd nor the
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

$Revision$

*/
package net.foxopen.foxydocs.model;

import net.foxopen.foxydocs.FoxyDocs;
import net.foxopen.foxydocs.model.abstractObject.AbstractDocumentedElement;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.jdom2.Element;

public class ModuleInformation extends AbstractDocumentedElement {

  private String oldContent;

  public ModuleInformation(Element node, AbstractModelObject parent) {
    super(parent, node, FoxyDocs.MODULE_FIELDS);
    // Set the oldContent to the current content
    save();
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for (EntryDoc e : elements) {
      buffer.append(e.getValue());
    }
    return buffer.toString();
  }

  @Override
  public synchronized boolean isDirty() {
    return !oldContent.equals(this.toString());
  }

  @Override
  public void save() {
    oldContent = this.toString();
  }

  @Override
  public boolean getHasChildren() {
    return false;
  }

  @Override
  public String getName() {
    return "Header";
  }

}
