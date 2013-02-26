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

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;
import static net.foxopen.foxydocs.FoxyDocs.NAMESPACE_FM;

import org.jdom2.Element;

public class ModuleInformation extends AbstractModelObject{

  private final static String[] attributeList = new String[] { "name", "title", "application-title", "version-desc", "description", "build-notes", "help-text", };

  private final HashMap<String, Element> attributes = new HashMap<String, Element>();

  private String oldContent;

  public ModuleInformation(Element node, AbstractModelObject parent) {
    super(parent);
    // Some extra fields
    for (String attr : attributeList) {
      Element attributeElement = node.getChild(attr, NAMESPACE_FM);
      if (attributeElement == null) {
        attributeElement = new Element(attr, NAMESPACE_FM);
        node.addContent(attributeElement);
      }
      attributes.put(attr, attributeElement);
    }

    save();
  }

  public Set<Entry<String, Element>> getContent() {
    return attributes.entrySet();
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for (Element e : attributes.values()) {
      buffer.append(e.getTextNormalize());
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

  public void change(String key, String text) {
    attributes.get(key).removeContent();
    attributes.get(key).addContent(text);
    parent.firePropertyChange("dirty", false, isDirty());
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    // No children
    return null;
  }

  @Override
  public String getName() {
   return getParent().getName();
  }

}
