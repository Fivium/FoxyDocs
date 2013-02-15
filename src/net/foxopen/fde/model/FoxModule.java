package net.foxopen.fde.model;

import static net.foxopen.utils.Constants.DOM_BUILDER;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.foxopen.fde.model.abstractObject.AbstractFSItem;
import net.foxopen.fde.model.abstractObject.AbstractModelObject;
import net.foxopen.utils.Constants;
import net.foxopen.utils.Logger;

import org.apache.commons.io.IOUtils;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

public class FoxModule extends AbstractFSItem {

  private final List<AbstractModelObject> documentationEntriesSet = new ArrayList<AbstractModelObject>();
  private String code;

  public FoxModule(File file, AbstractFSItem parent) {
    super(file, parent);
    checkFile();
    if (!f_file.isFile())
      throw new IllegalArgumentException("A Fox Module must be a file");
    if (!f_file.getName().toUpperCase().endsWith(".XML"))
      throw new IllegalArgumentException("Invalid XML file " + f_file.getName());
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
  public synchronized void readContent() throws ParserConfigurationException, SAXException, IOException, JDOMException, NotAFoxModuleException {
    Logger.logStdout("Loading module " + f_file.getAbsolutePath());

    org.jdom2.Document jdomDoc = DOM_BUILDER.build(f_file);

    // Entries
    addEntries(documentationEntriesSet, jdomDoc, "Header", "//fm:header");
    addEntries(documentationEntriesSet, jdomDoc, "Entry Themes", "//fm:entry-theme");
    addEntries(documentationEntriesSet, jdomDoc, "Actions", "//fm:action");
    addEntries(documentationEntriesSet, jdomDoc, "Orphans", "//*[./fm:documentation and name()!='fm:header' and name()!='fm:entry-theme' and name()!='fm:action']");

    if (documentationEntriesSet.size() == 0) {
      throw new NotAFoxModuleException(f_file.getName());
    }
    FileInputStream inputStream = new FileInputStream(f_file);
    try {
      code = IOUtils.toString(inputStream);
    } finally {
        inputStream.close();
    }
    //XML_SERIALISER.outputString(jdomDoc);
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
  public List<FoxModule> getFoxModules() {
    ArrayList<FoxModule> buffer = new ArrayList<FoxModule>();
    buffer.add(this);
    return buffer;
  }

  @Override
  public String getCode() {
    return code;
  }

  public static List<Element> runXpath(String xpath, org.jdom2.Document document) {
    XPathExpression<Element> actionsXPath = XPathFactory.instance().compile(xpath, Filters.element(), null, Constants.NAMESPACE_FM);
    List<Element> results = actionsXPath.evaluate(document);
    return results;
  }

  public static class NotAFoxModuleException extends Exception {
    private static final long serialVersionUID = -2209351415786369112L;

    public NotAFoxModuleException(String name) {
      super(name + " is not a valid FoxModule");
    }
  }

}
