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
package net.foxopen.foxydocs.utils;

import static net.foxopen.foxydocs.utils.ResourceManager.*;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import net.foxopen.foxydocs.FoxyDocs;
import net.foxopen.foxydocs.model.FoxModule;
import net.foxopen.foxydocs.model.abstractObject.AbstractFSItem;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.xml.sax.SAXException;

public class Export {

  private static final FopFactory fopFactory = FopFactory.newInstance();

  public static void openWebpage(URI uri) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(uri);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static List<Content> transform(Document doc, String stylesheet) throws JDOMException, FileNotFoundException {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(getInternalFile(stylesheet)));
      JDOMSource in = new JDOMSource(doc);
      JDOMResult out = new JDOMResult();
      transformer.transform(in, out);
      return out.getResult();
    } catch (TransformerException e) {
      throw new JDOMException("XSLT Transformation failed", e);
    }
  }

  public static Document foxToHtml(File module) throws ParserConfigurationException, JDOMException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    factory.setNamespaceAware(false);
    DocumentBuilder builder = factory.newDocumentBuilder();

    DOMBuilder jbuilder = new DOMBuilder();
    Document doc = jbuilder.build(builder.parse(module));

    List<Content> res = transform(doc, "xsl/module.xsl");
    Document resDoc = new Document();

    resDoc.addContent(new Element("html"));
    resDoc.getRootElement().addContent(res);

    return resDoc;
  }

  public static SingleExportThread toPDF(final File foxModule, final File targetDirectory) {
    return new SingleExportThread(new SingleExport() {
      @Override
      public void run() throws Exception {
        targetDirectory.mkdir();
        duplicateResource("xsl/logo.png", targetDirectory.getAbsolutePath() + "/logo.png");
        duplicateResource("xsl/style.css", targetDirectory.getAbsolutePath() + "/style.css");

        File out = new File(targetDirectory.getAbsolutePath() + "/module.pdf");

        toPDFinternal(foxModule, out);
      }
    });
  }

  private static void toPDFinternal(File foxModule, File out) throws ParserConfigurationException, JDOMException, SAXException, IOException, TransformerException {

    Document html = foxToHtml(foxModule);

    List<Content> res = transform(html, "xsl/xhtml2fo.xsl");
    Document fopDoc = new Document(res);

    OutputStream pdfout = new BufferedOutputStream(new FileOutputStream(out));

    try {
      Fop fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, pdfout);
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      JDOMSource src = new JDOMSource(fopDoc);
      Result pdfres = new SAXResult(fop.getDefaultHandler());
      transformer.transform(src, pdfres);

    } finally {
      // Clean-up
      pdfout.close();
    }

    openWebpage(out.toURI());
  }

  public static IRunnableWithProgress toHTML(AbstractFSItem root, File targetDirectory) {
    Collection<FoxModule> modules = root.getFoxModules().values();
    Logger.logStdout(modules.size() + " modules selected");
    return new MultiExportThread(modules, targetDirectory);
  }

  private static class MultiExportThread implements IRunnableWithProgress {

    private final List<FoxModule> modules;
    private final File targetDirectory;
    private final File indexFile;
    private final Document moduleListXML = new Document();

    public MultiExportThread(Collection<FoxModule> modules, File targetDiretory) {
      this.modules = new ArrayList<FoxModule>(modules);
      this.targetDirectory = targetDiretory;
      this.indexFile = new File(targetDirectory.getAbsolutePath() + "/index.html");

      moduleListXML.addContent(new Element("MODULE_LIST"));
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      monitor.beginTask("Generating Documentation", modules.size() + 3);

      try {
        // Create the target directory
        targetDirectory.mkdir();
        monitor.worked(1);
        // Copy static resources across

        duplicateResource("xsl/index.html", indexFile);
        duplicateResource("xsl/logo.png", targetDirectory.getAbsolutePath() + "/logo.png");
        duplicateResource("xsl/bg.png", targetDirectory.getAbsolutePath() + "/bg.png");
        duplicateResource("xsl/style.css", targetDirectory.getAbsolutePath() + "/style.css");
        duplicateResource("xsl/summary.html", targetDirectory.getAbsolutePath() + "/summary.html");
        monitor.worked(1);

        // Sort the collection
        Collections.sort(modules, AbstractModelObject.getComparator());
        // Generate HTML for each module
        for (FoxModule module : modules) {
          if (monitor.isCanceled())
            break;

          // Generate the HTML document
          Document html = foxToHtml(module.getFile());

          // Write it
          File targetFile = new File(targetDirectory.getAbsolutePath() + "/" + module.getName() + ".html");
          targetFile.createNewFile();
          FileOutputStream outhtml = new FileOutputStream(targetFile, false);
          FoxyDocs.getSerialiser().output(html, outhtml);
          outhtml.close();

          // Add the module to the list
          Element moduleElement = new Element("MODULE");
          moduleElement.addContent(module.getName());
          moduleListXML.getRootElement().addContent(moduleElement);

          monitor.worked(1);
        }

        // Generate listing
        Document listing = new Document(transform(moduleListXML, "xsl/listing.xsl"));
        FileOutputStream outhtml = new FileOutputStream(new File(targetDirectory.getAbsolutePath() + "/listing.html"), false);
        FoxyDocs.getSerialiser().output(listing, outhtml);
        outhtml.close();
        monitor.worked(1);

      } catch (Exception e) {
        e.printStackTrace();
      }

      monitor.done();
      if (monitor.isCanceled()) {
        throw new InterruptedException("The long running operation was cancelled");
      }

      openWebpage(indexFile.toURI());
    }
  }

  private static interface SingleExport {
    public void run() throws Exception;
  }

  private static class SingleExportThread implements IRunnableWithProgress {

    private final SingleExport task;

    public SingleExportThread(SingleExport task) {
      this.task = task;
    }

    @Override
    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      monitor.beginTask("Generating Documentation", IProgressMonitor.UNKNOWN);

      try {
        task.run();
        monitor.worked(1);
      } catch (Exception e) {
        e.printStackTrace();
      }

      monitor.done();
      if (monitor.isCanceled()) {
        throw new InterruptedException("The long running operation was cancelled");
      }
    }
  }
}
