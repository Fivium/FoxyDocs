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

import org.jdom2.Element;

public class DocEntry {

  private final Element node;

  private Element comments;
  private Element description;
  private Element precondition;

  public DocEntry(Element node) {
    this.node = node;
    comments = node.getChild("comments", NAMESPACE_FM);
    description = node.getChild("description", NAMESPACE_FM);
    precondition = node.getChild("pre-condition", NAMESPACE_FM);
  }

  public boolean isEmpty() {
    return description.getTextTrim().length() == 0;
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

  public DocEntry setDocumentation(String content) {
    description.removeContent();
    description.addContent(content);
    return this;
  }

  @Override
  public String toString() {
    if (description == null) {
      return "";
    }
    return description.getTextNormalize();
  }

}
