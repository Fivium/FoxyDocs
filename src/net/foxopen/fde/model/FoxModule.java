package net.foxopen.fde.model;

import static net.foxopen.utils.FoxyDocs.DOM_BUILDER;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.foxopen.fde.model.abstractObject.AbstractFSItem;
import net.foxopen.fde.model.abstractObject.AbstractModelObject;
import net.foxopen.utils.FoxyDocs;
import net.foxopen.utils.Logger;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

public class FoxModule extends AbstractFSItem {

  private final List<AbstractModelObject> documentationEntriesSet = new ArrayList<AbstractModelObject>();
  private Document jdomDoc;

  public FoxModule(Path path, AbstractFSItem parent) throws IOException, NotAFoxModuleException {
    super(path, parent);
    checkFile();
    if (!internalPath.toFile().isFile())
      throw new IllegalArgumentException("A Fox Module must be a file");
    if (!getPath().toUpperCase().endsWith(".XML"))
      throw new NotAFoxModuleException("Invalid XML file " + getPath());
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
  public synchronized Collection<AbstractFSItem> readContent() throws ParserConfigurationException, SAXException, IOException, JDOMException, NotAFoxModuleException {
    Logger.logStdout("Loading module " + getPath());

    jdomDoc = DOM_BUILDER.build(internalPath.toFile());

    // Entries
    addEntries(documentationEntriesSet, jdomDoc, "Header", "//fm:header");
    addEntries(documentationEntriesSet, jdomDoc, "Entry Themes", "//fm:entry-theme");
    addEntries(documentationEntriesSet, jdomDoc, "Actions", "//fm:action");
    addEntries(documentationEntriesSet, jdomDoc, "Orphans", "//*[./fm:documentation and name()!='fm:header' and name()!='fm:entry-theme' and name()!='fm:action']");

    if (documentationEntriesSet.size() == 0) {
      throw new NotAFoxModuleException(getName());
    }

    return null;
  }

  private void addEntries(List<AbstractModelObject> data, org.jdom2.Document document, String key, String xpath) {
    DocumentationEntriesSet set = new DocumentationEntriesSet(key, this);
    for (Element e : runXpath(xpath, document)) {
      set.add(new DocumentationEntry(e, set));
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
    buffer.put(getPath(), this);
    return buffer;
  }

  @Override
  public String getCode() {
    try {
      FileInputStream inputStream = new FileInputStream(internalPath.toFile());
      String buffer = IOUtils.toString(inputStream);
      inputStream.close();
      return buffer;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return toString();
  }

  @Override
  public String toString() {
    return FoxyDocs.XML_SERIALISER.outputString(jdomDoc);
  }
  
  @Override
  public void save(){
    super.save();
    reload();
   // System.out.println(this);
  }

  public static List<Element> runXpath(String xpath, org.jdom2.Document document) {
    XPathExpression<Element> actionsXPath = XPathFactory.instance().compile(xpath, Filters.element(), null, FoxyDocs.NAMESPACE_FM);
    return actionsXPath.evaluate(document);
  }

  public static class NotAFoxModuleException extends Exception {
    private static final long serialVersionUID = -2209351415786369112L;

    public NotAFoxModuleException(String name) {
      super(name + " is not a valid FoxModule");
    }
  }
}
