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

import static net.foxopen.foxydocs.FoxyDocs.*;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.foxopen.foxydocs.model.abstractObject.AbstractFSItem;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.eclipse.swt.widgets.Display;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

public class FoxModule extends AbstractFSItem {

  private final List<AbstractModelObject> documentationEntriesSet = Collections.synchronizedList(new ArrayList<AbstractModelObject>());
  
  private Document jdomDoc;
  private ModuleInformation moduleInfo;
  private String fullStringContent;
  private ArrayList<DocumentedElement> docElements = new ArrayList<>();

  public FoxModule(Path path, AbstractFSItem parent) throws NotAFoxModuleException, IOException {
    super(path, parent);
    checkFile();

    // Check type
    String type = Files.probeContentType(path);
    if (type == null || !type.endsWith("xml"))
      throw new NotAFoxModuleException("Invalid XML file " + getAbsolutePath());
  }

  /**
   * Parse the XML content
   * 
   * @param internalPath
   *          the input file
   * @return the data structure with the code and the documentation entry
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   * @throws JDOMException
   * @throws NotAFoxModuleException
   */
  @Override
  public Collection<AbstractFSItem> readContent() throws ParserConfigurationException, SAXException, IOException, JDOMException, NotAFoxModuleException {
    // Parse the document two times to have a proper location for each element
    jdomDoc = DOM_BUILDER.build(new ByteArrayInputStream(XML_SERIALISER.outputString(DOM_BUILDER.build(getFile())).getBytes("UTF-8")));

    // Cache the module as a String while parsing so opening a tab is faster
    fullStringContent = XML_SERIALISER.outputString(jdomDoc);

    // Header
    List<Element> header = runXpath(FOX_MODULE_XPATH + "fm:header", jdomDoc);
    if (header.size() != 1) {
      throw new NotAFoxModuleException(getName());
    }
    DocumentedElement headerElement = new DocumentedElement(header.get(0), this, true);
    documentationEntriesSet.add(headerElement);
    docElements.add(headerElement);
   
    // Module informations (header content)
    moduleInfo = new ModuleInformation(header.get(0), headerElement);

    // Entry themes
    addEntries(documentationEntriesSet, jdomDoc, "Entry Themes", FOX_MODULE_XPATH + "fm:entry-theme-list/fm:entry-theme");

    // Module actions
    addEntries(documentationEntriesSet, jdomDoc, "Module Actions", FOX_MODULE_XPATH + "fm:action-list/fm:action");

    // States actions
    List<Element> states = runXpath(FOX_MODULE_XPATH + "fm:state-list/fm:state", jdomDoc);
    for (Element state : states) {
      String name = state.getAttributeValue("name");
      addEntries(documentationEntriesSet, jdomDoc, "State " + name + " Actions", FOX_MODULE_XPATH + "/fm:state-list/fm:state[@name='" + name + "']//fm:action");
    }

    if (documentationEntriesSet.size() == 0) {
      throw new NotAFoxModuleException(getName());
    }

    return null;
  }

  private void addEntries(List<AbstractModelObject> data, org.jdom2.Document document, String key, String xpath) {
    DocumentedElementSet set = new DocumentedElementSet(key, this);
    for (Element e : runXpath(xpath, document)) {
      DocumentedElement docElement = new DocumentedElement(e, set);
      // Own set
      set.add(docElement);

      // Flatted version
      docElements.add(docElement);
    }
    if (set.size() > 0) {
      data.add(set);
    }
  }

  public void delete() {
    getParent().getChildren().remove(this);
    getParent().firePropertyChange("children", null, getParent().getChildren());
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    return documentationEntriesSet;
  }

  @Override
  public HashMap<String, FoxModule> getFoxModules() {
    HashMap<String, FoxModule> buffer = new HashMap<String, FoxModule>();
    buffer.put(getAbsolutePath(), this);
    return buffer;
  }

  @Override
  public String getCode() {
    return fullStringContent;
  }

  @Override
  public String toString() {
    return getName();
  }
  
  @Override
  public synchronized boolean isDirty(){
    return super.isDirty() || (moduleInfo != null && moduleInfo.isDirty());
  }

  @Override
  public void save() throws Exception {
    if (!isDirty())
      return;
    if (isReadOnly()) {
      throw new IOException("You cannot save this file : This file is locked or you don't have access to it.");
    }
    // Prepare the save
    super.save();
    moduleInfo.save();

    // Write into the file
    FileOutputStream out = new FileOutputStream(getFile(), false);
    XML_SERIALISER.output(jdomDoc, out);
    out.close();
    
    // Update the content
    reload();
    jdomDoc = DOM_BUILDER.build(getFile());
    fullStringContent = XML_SERIALISER.outputString(jdomDoc);
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        firePropertyChange("code", null, getCode());
      }
    });
  }

  public ArrayList<DocumentedElement> getAllEntries() {
    return docElements;
  }

  public static List<Element> runXpath(String xpath, org.jdom2.Document document) {
    XPathExpression<Element> actionsXPath = XPathFactory.instance().compile(xpath, Filters.element(), null, NAMESPACE_FM, NAMESPACE_XS);
    return actionsXPath.evaluate(document);
  }

  public static class NotAFoxModuleException extends Exception {
    private static final long serialVersionUID = -2209351415786369112L;

    public NotAFoxModuleException(String name) {
      super(name + " is not a valid FoxModule");
    }
  }

  public ModuleInformation getModuleInfo() {
    return moduleInfo;
  }
}
