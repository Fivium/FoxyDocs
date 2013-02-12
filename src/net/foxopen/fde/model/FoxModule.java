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

  private final List<AbstractModelObject> documentationEntriesSet;
  private File f_file;
  
  public FoxModule(String path, AbstractModelObject parent) throws IOException, JDOMException, ParserConfigurationException, SAXException, NotAFoxModuleException {
    Logger.logStdout("Loading module " + path);
    
    this.parent = parent;
    
    // Open the file in the FS
    this.f_file = new java.io.File(path);

    // Can we read the file ?
    if (!f_file.canRead()) {
      throw new IOException("Cannot read " + f_file.getAbsolutePath());
    }

    // If so, parse the content
    documentationEntriesSet = parseContent(f_file);
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
    return f_file.getName()+" "+(isDirty()?"*":"");
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
  private List<AbstractModelObject> parseContent(File f_file) throws ParserConfigurationException, SAXException, IOException, JDOMException, NotAFoxModuleException {
    // Parse the data
    List<AbstractModelObject> data = new ArrayList<AbstractModelObject>();
    DocumentBuilderFactory domfactory = DocumentBuilderFactory.newInstance();
    DOMBuilder builder = new DOMBuilder();
    domfactory.setNamespaceAware(false);

    DocumentBuilder dombuilder = domfactory.newDocumentBuilder();
    org.w3c.dom.Document domDoc = dombuilder.parse(f_file);
    org.jdom2.Document jdomDoc = builder.build(domDoc);
    Logger.logStdout("File " + f_file + " loaded to " + jdomDoc);

    // Entries
    addEntries(data, jdomDoc, "Header", "//fm:header");
    addEntries(data, jdomDoc, "Entry Themes", "//fm:entry-theme");
    addEntries(data, jdomDoc, "Actions", "//fm:action");
    addEntries(data, jdomDoc, "Orphanes", "//*[./fm:documentation and name()!='fm:header' and name()!='fm:entry-theme' and name()!='fm:action']");
    
    if (data.size() == 0){
      throw new NotAFoxModuleException(f_file.getName());
    }

    return data;
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

    public NotAFoxModuleException(String name){
      super(name+" is not a valid FoxModule");
    }
  }
 
}
