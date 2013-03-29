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

import static net.foxopen.foxydocs.FoxyDocs.NAMESPACE_FM;

import org.jdom2.Element;

import net.foxopen.foxydocs.FoxyDocs;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

public class EntryDoc extends AbstractModelObject {

  private Element node;
  private String attr;
  private boolean mandatory = false;

  public EntryDoc(AbstractModelObject parent, Element parentNode, String attr) {
    super(parent);

    if (attr.startsWith("*")) {
      mandatory = true;
      attr = attr.substring(1);
    }

    this.attr = attr;
    this.node = parentNode.getChild(attr, NAMESPACE_FM);
    if (this.node == null) {
      this.node = new Element(attr, NAMESPACE_FM);
      parentNode.addContent(node);
    }
  }

  @Override
  public synchronized int getStatus() {
    if (!mandatory)
      return FoxyDocs.STATUS_UNKNOWN;
    if (node == null || node.getTextTrim().length() == 0)
      return FoxyDocs.STATUS_MISSING;
    return node.getContentSize() > 0 ? FoxyDocs.STATUS_OK : FoxyDocs.STATUS_MISSING;
  }

  @Override
  public String getName() {
    return (mandatory ? "* " : "") + attr.substring(0, 1).toUpperCase() + attr.substring(1);
  }

  public void setValue(String text) {
    node.removeContent();
    if (text != null && text.trim().length() > 0)
      node.addContent(text);

    firePropertyChange("dirty", false, isDirty());
    firePropertyChange("status", FoxyDocs.STATUS_UNKNOWN, getStatus());
  }

  public String getKey() {
    return attr;
  }

  public String getValue() {
    if (node == null)
      return "";
    return node.getTextNormalize();
  }

  public static String getDisplayedName(String key) {
    StringBuffer buffer = new StringBuffer();
    if (key.startsWith("*")) {
      buffer.append("* ");
      key = key.substring(1);
    }
    buffer.append(key.substring(0, 1).toUpperCase());
    buffer.append(key.substring(1));
    return buffer.toString();
  }

}
