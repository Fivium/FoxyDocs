package net.foxopen.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

public class Export {

  private static final FopFactory fopFactory = FopFactory.newInstance();
  public static void toPDF(String rawXML) throws FOPException, TransformerException, IOException {

    OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("test.pdf")));
    

    try {
      @SuppressWarnings("static-access")
      Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      Source src = new StreamSource(new StringReader(rawXML));
      Result res = new SAXResult(fop.getDefaultHandler());
      transformer.transform(src, res);

    } finally {
      // Clean-up
      out.close();
    }
  }
}
