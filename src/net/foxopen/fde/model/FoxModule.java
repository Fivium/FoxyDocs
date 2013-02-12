package net.foxopen.fde.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.foxopen.fde.model.abstractObject.AbstractFSItem;
import net.foxopen.fde.model.abstractObject.AbstractModelObject;
import net.foxopen.utils.Logger;
import net.foxopen.utils.XPath;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.xml.sax.SAXException;

public class FoxModule extends AbstractFSItem {

  private final List<AbstractModelObject> documentationEntriesSet = new ArrayList<AbstractModelObject>();

  public FoxModule(File file, AbstractFSItem parent) {
    super(file, parent);
    checkFile();
    if (!f_file.isFile())
      throw new IllegalArgumentException("A Fox Module must be a file");
    if (!f_file.getName().toUpperCase().endsWith(".XML"))
      throw new IllegalArgumentException("Invalid XML file " + f_file.getName());
  }

  /**
   * Is the file read only ?
   * 
   * @return true if the file is not writable, false otherwise
   * @throws IOException
   */
  public boolean isReadOnly() throws IOException {
    if (!f_file.canRead()) {
      throw new IOException("Cannot read " + f_file.getAbsolutePath());
    }
    return !f_file.canWrite();
  }

  public String getName() {
    return f_file.getName() + " " + (isDirty() ? "*" : "");
  }

  /**
   * Parse the XML content
   * 
   * @param f_file
   *          the input file
   * @return the data structure with the code and the documentation entry
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   * @throws JDOMException
   * @throws NotAFoxModuleException
   */
  public void readContent() throws ParserConfigurationException, SAXException, IOException, JDOMException, NotAFoxModuleException {
    Logger.logStdout("Loading module " + f_file.getAbsolutePath());
    // Parse the data
    DocumentBuilderFactory domfactory = DocumentBuilderFactory.newInstance();
    DOMBuilder builder = new DOMBuilder();
    domfactory.setNamespaceAware(false);

    DocumentBuilder dombuilder = domfactory.newDocumentBuilder();
    org.w3c.dom.Document domDoc = dombuilder.parse(f_file);
    org.jdom2.Document jdomDoc = builder.build(domDoc);
    Logger.logStdout("File " + f_file + " loaded to " + jdomDoc);

    // Entries
    addEntries(documentationEntriesSet, jdomDoc, "Header", "//fm:header");
    addEntries(documentationEntriesSet, jdomDoc, "Entry Themes", "//fm:entry-theme");
    addEntries(documentationEntriesSet, jdomDoc, "Actions", "//fm:action");
    addEntries(documentationEntriesSet, jdomDoc, "Orphanes", "//*[./fm:documentation and name()!='fm:header' and name()!='fm:entry-theme' and name()!='fm:action']");

    if (documentationEntriesSet.size() == 0) {
      throw new NotAFoxModuleException(f_file.getName());
    }
  }

  private DocumentationEntriesSet parse(org.jdom2.Document document, String key, String xpath) {
    // Actions
    DocumentationEntriesSet set = new DocumentationEntriesSet(key, this);
    for (Element e : XPath.run(xpath, document)) {
      set.add(new DocumentationEntry(e, set));
    }
    return set;
  }

  private void addEntries(List<AbstractModelObject> data, org.jdom2.Document document, String key, String xpath) {
    DocumentationEntriesSet tmp = parse(document, key, xpath);
    if (tmp.size() > 0) {
      data.add(tmp);
    }
  }
  
  public void delete(){
    getParent().getChildren().remove(this);
    getParent().cascadeFirePropertyChange("children", null, getParent().getChildren());
  }

  @Override
  public List<AbstractModelObject> getChildren() {
    return documentationEntriesSet;
  }

  @Override
  public List<FoxModule> getFoxModules() {
    ArrayList<FoxModule> buffer = new ArrayList<FoxModule>();
    buffer.add(this);
    return buffer;
  }

  public static class NotAFoxModuleException extends Exception {
    private static final long serialVersionUID = -2209351415786369112L;

    public NotAFoxModuleException(String name) {
      super(name + " is not a valid FoxModule");
    }
  }

}
