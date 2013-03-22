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

import static net.foxopen.foxydocs.FoxyDocs.FOX_MODULE_XPATH;
import static net.foxopen.foxydocs.FoxyDocs.getDOMBuilder;
import static net.foxopen.foxydocs.FoxyDocs.getSerialiser;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.foxopen.foxydocs.FoxyDocs;
import net.foxopen.foxydocs.analysers.ScopeAnalyser;
import net.foxopen.foxydocs.model.abstractObject.AbstractDocumentedElement;
import net.foxopen.foxydocs.model.abstractObject.AbstractFSItem;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Display;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

public class FoxModule extends AbstractFSItem {

  private Document jdomDoc;
  private ModuleInformation moduleInfo;
  private String fullStringContent;
  private List<String> scope = new ArrayList<String>();

  public FoxModule(Path path, AbstractFSItem parent) throws NotAFoxModuleException, IOException {
    super(path, parent);
    checkFile();

    // Check type
    String type = Files.probeContentType(path);
    if (type == null || !type.endsWith("xml"))
      throw new NotAFoxModuleException("Invalid XML file " + getAbsolutePath());
  }

  private DocumentedElementSet addEntries(String key, String xpath) {
    DocumentedElementSet set = new DocumentedElementSet(key, this);

    for (Element e : runXpath(xpath, jdomDoc)) {
      AbstractDocumentedElement docElement = new DocumentedElement(e, set);
      set.addChild(docElement);
    }
    if (set.getHasChildren()) {
      addChild(set);
    }

    return set;
  }

  public ArrayList<AbstractDocumentedElement> getAllEntries() {
    ArrayList<AbstractDocumentedElement> buffer = new ArrayList<AbstractDocumentedElement>();
    buffer.add(moduleInfo);
    for (AbstractModelObject o : getChildren()) {
      for (AbstractDocumentedElement c : o.getChildren(AbstractDocumentedElement.class)) {
        buffer.add(c);
      }
    }
    return buffer;
  }

  public AbstractDocumentedElement getModuleInfo() {
    return moduleInfo;
  }

  @Override
  public String getName() {
    return getFile().getName().replaceAll(".xml", "");
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
  public synchronized boolean isDirty() {
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
    fullStringContent = stripNamespacesUnicity(FoxyDocs.getSerialiser().outputString(jdomDoc));
    FileUtils.writeStringToFile(getFile(), fullStringContent, "UTF-8");

    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        firePropertyChange("module", null, this);
        firePropertyChange("code", null, getCode());
      }
    });
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
  public synchronized Collection<AbstractFSItem> readContent() throws NotAFoxModuleException {
    try {
      reload();
      // Patch raw file to handle duplicate namespaces
      String rawFile = FileUtils.readFileToString(getFile());
      rawFile = fixNamespacesUnicity(rawFile);

      // Parse the document two times to have a proper location for each element
      Document firstRun = getDOMBuilder().build(new StringReader(rawFile));
      rawFile = getSerialiser().outputString(firstRun);
      jdomDoc = getDOMBuilder().build(new StringReader(rawFile));

      // Cache the module as a String while parsing so opening a tab is faster
      // Strip the namespace hack of the displayed file
      fullStringContent = stripNamespacesUnicity(rawFile);

      // Reset data structures
      getChildren().clear();

    } catch (IOException | JDOMException e) {
      System.err.println(getFile().getName() + ": " + e.getMessage());
      throw new NotAFoxModuleException(getFile().getName());
    }

    // Header
    List<Element> header = runXpath(FOX_MODULE_XPATH + "fm:header", jdomDoc);
    if (header.size() != 1) {
      throw new NotAFoxModuleException(getName());
    }

    // Module informations (header content)
    moduleInfo = new ModuleInformation(header.get(0), this);
    addChild(moduleInfo);

    // Entry themes
    addEntries("Entry Themes", FOX_MODULE_XPATH + "fm:entry-theme-list/fm:entry-theme");

    // Module actions
    addEntries("Module Actions", FOX_MODULE_XPATH + "fm:action-list/fm:action");

    // States actions
    List<Element> states = runXpath(FOX_MODULE_XPATH + "fm:state-list/fm:state", jdomDoc);
    for (Element state : states) {
      String name = state.getAttributeValue("name");
      addEntries("State " + name + " Actions", FOX_MODULE_XPATH + "/fm:state-list/fm:state[@name='" + name + "']//fm:action");
    }

    // Module calls
    // TODO

    if (getChildren().size() == 0) {
      throw new NotAFoxModuleException(getName());
    }

    // Extract libraries
    List<Element> libraries = runXpath(FOX_MODULE_XPATH + "/fm:library-list/fm:library", jdomDoc);
    for (Element e : libraries) {
      scope.add(e.getTextNormalize());
    }

    return null;
  }

  public DocumentedElement getGlobalAction(String name) {
    for (DocumentedElementSet es : getChildren(DocumentedElementSet.class)) {
      if (es.getName().equals("Module Actions")) {
        for (DocumentedElement e : es.getChildren(DocumentedElement.class)) {
          if (e.getName().equals(name)) {
            return e;
          }
        }
      }
    }
    return null;
  }

  public List<String> getLibraries() {
    return scope;
  }

  public String stripNamespacesUnicity(String input) {
    return input.replaceAll("xmlns:(.+?)=\"(.+?)/\\1\"", "xmlns:$1=\"$2\"");
  }

  public String fixNamespacesUnicity(String input) {
    return input.replaceAll("xmlns:(.+?)=\"(.+?)\"", "xmlns:$1=\"$2/$1\"");
  }

  public static class NotAFoxModuleException extends Exception {
    private static final long serialVersionUID = -2209351415786369112L;

    public NotAFoxModuleException(String name) {
      super(name + " is not a valid FoxModule");
    }
  }

  public List<DocumentedElement> getActions() {
    List<DocumentedElement> actions = new ArrayList<>();

    for (DocumentedElementSet e : getChildren(DocumentedElementSet.class)) {
      for (DocumentedElement doc : e.getChildren(DocumentedElement.class)) {
        actions.addAll(doc.getActions());
      }
    }

    return actions;
  }
}
