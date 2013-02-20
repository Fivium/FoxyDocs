package net.foxopen.foxydocs.model;

import static net.foxopen.foxydocs.FoxyDocs.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.foxopen.foxydocs.model.abstractObject.AbstractFSItem;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.widgets.Display;
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
  private HeaderElement headerElement ;

  public FoxModule(Path path, AbstractFSItem parent) throws NotAFoxModuleException {
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
  @Override
  public synchronized Collection<AbstractFSItem> readContent() throws ParserConfigurationException, SAXException, IOException, JDOMException, NotAFoxModuleException {
    jdomDoc = DOM_BUILDER.build(internalPath.toFile());

    // Header
    List<Element> header = runXpath("/xs:schema/xs:annotation/xs:appinfo/fm:module/fm:header", jdomDoc);
    if (header.size() != 1) {
      throw new NotAFoxModuleException(getName());
    }
    headerElement = new HeaderElement(header.get(0), this);
    documentationEntriesSet.add(headerElement);

    // Regular entries
    addEntries(documentationEntriesSet, jdomDoc, "Entry Themes", "/xs:schema/xs:annotation/xs:appinfo/fm:module//fm:entry-theme");
    addEntries(documentationEntriesSet, jdomDoc, "Actions", "/xs:schema/xs:annotation/xs:appinfo/fm:module//fm:action");
    addEntries(documentationEntriesSet, jdomDoc, "Orphans", "/xs:schema/xs:annotation/xs:appinfo/fm:module//*[./fm:documentation and name()!='fm:header' and name()!='fm:entry-theme' and name()!='fm:action']");

    if (documentationEntriesSet.size() == 0) {
      throw new NotAFoxModuleException(getName());
    }

    return null;
  }

  private void addEntries(List<AbstractModelObject> data, org.jdom2.Document document, String key, String xpath) {
    DocumentedElementSet set = new DocumentedElementSet(key, this);
    for (Element e : runXpath(xpath, document)) {
      set.add(new DocumentedElement(e, set));
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
    return XML_SERIALISER.outputString(jdomDoc);
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

    // Write into the file
    FileOutputStream out = new FileOutputStream(internalPath.toFile(), false);
    XML_SERIALISER.output(jdomDoc, out);
    out.close();

    // Update the content
    reload();
    jdomDoc = DOM_BUILDER.build(internalPath.toFile());
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        firePropertyChange("code", null, getCode());
      }
    });
  }

  public ArrayList<AbstractModelObject> getAllEntries() {
    ArrayList<AbstractModelObject> buffer = new ArrayList<AbstractModelObject>();
    for (AbstractModelObject child : getChildren()) {
      if (child.getHasChildren())
        buffer.addAll(child.getChildren());
      else
        buffer.add(child);
    }
    return buffer;
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
}
