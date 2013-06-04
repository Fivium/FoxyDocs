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

 */
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