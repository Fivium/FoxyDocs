package net.foxopen.fde.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.foxopen.utils.Logger;
import net.foxopen.utils.XPath;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.xml.sax.SAXException;

public class FoxModule extends AbstractModelObject {

  private final List<AbstractModelObject> documentationEntriesSet = new ArrayList<AbstractModelObject>();
  private final File f_file;

  public FoxModule(File file, AbstractModelObject parent) {
    this.parent = parent;
    this.f_file = file;
  }

  public void read() throws IOException, JDOMException, ParserConfigurationException, SAXException {
    Logger.logStdout("Loading module " + f_file.getAbsolutePath());

    // Can we read the file ?
    if (!f_file.canRead()) {
      throw new IOException("Cannot read " + f_file.getAbsolutePath());
    }

    // If so, parse the content
    try {
      parseContent(f_file);
    } catch (NotAFoxModuleException e) {
      Logger.logStderr(e.getMessage());
    }
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
  private void parseContent(File f_file) throws ParserConfigurationException, SAXException, IOException, JDOMException, NotAFoxModuleException {
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

  @Override
  public List<AbstractModelObject> getChildren() {
    return documentationEntriesSet;
  }

  public static class NotAFoxModuleException extends Exception {
    private static final long serialVersionUID = -2209351415786369112L;

    public NotAFoxModuleException(String name) {
      super(name + " is not a valid FoxModule");
    }
  }

}
