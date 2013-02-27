package net.foxopen.utils;

import static net.foxopen.foxydocs.FoxyDocs.XML_SERIALISER;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
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

  public static List<Content> transform(Document doc, String stylesheet) throws JDOMException {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(stylesheet));
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

    List<Content> res = transform(doc, "assets/xsl/module.xsl");
    Document resDoc = new Document();

    resDoc.addContent(new Element("html"));
    resDoc.getRootElement().addContent(res);

    return resDoc;
  }

  public static void toPDF(File foxModule, File out) throws ParserConfigurationException, JDOMException, SAXException, IOException, TransformerException {
    Document html = foxToHtml(foxModule);
    
    // Save html dump
    FileOutputStream outhml = new FileOutputStream(new File("out.html"), false);
    XML_SERIALISER.output(html, outhml);
    outhml.close();

    List<Content> res = transform(html, "assets/xsl/xhtml2fo.xsl");
    Document fopDoc = new Document(res);

    OutputStream pdfout = new BufferedOutputStream(new FileOutputStream(out));

    try {
      @SuppressWarnings("static-access")
      Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, pdfout);
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      Source src = new StreamSource(new StringReader(XML_SERIALISER.outputString(fopDoc)));
      Result pdfres = new SAXResult(fop.getDefaultHandler());
      transformer.transform(src, pdfres);

    } finally {
      // Clean-up
      pdfout.close();
    }
  }
}
