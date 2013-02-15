package net.foxopen.utils;

import net.foxopen.fde.view.FDEMainWindow;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.ResourceManager;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Constants {

  private Constants() {
    // Empty
  }

  public static void init() {
    Logger.logStdout("Init constants");
  }

  public final static int STATUS_UNKNOWN = 0x0;
  public final static int STATUS_MISSING = 0x01;
  public final static int STATUS_OK = 0x02;
  public final static int STATUS_PARTIAL = 0x03;

  public static final Image IMAGE_OK = getImage("/img/actions/ok.png");
  public static final Image IMAGE_MISSING = getImage("/img/actions/cancel.png");
  public static final Image IMAGE_PARTIAL = getImage("/img/actions/edit_remove.png");
  public static final Image IMAGE_UNKNOWN = getImage("/img/actions/messagebox_question.png");

  private static Image getImage(String file) {
    ImageDescriptor image = ResourceManager.getImageDescriptor(FDEMainWindow.class, file);
    return image.createImage();
  }

  private static final Device device = Display.getCurrent();
  public static final Color GREY = new Color(device, 200, 200, 200);
  public static final Color RED = new Color(device, 255, 0, 0);
  public static final Color DARK_RED = new Color(device, 200, 50, 50);
  public static final Color GREEN = new Color(device, 0, 255, 0);
  public static final Color CYAN = new Color(device, 0, 255, 255);
  public static final Color BLUE = new Color(device, 0, 0, 255);
  public static final Color PURPLE = new Color(device, 255, 0, 255);
  public static final Color YELLOW = new Color(device, 255, 255, 0);

  public static final Font FONT_DEFAULT = new Font(Display.getCurrent(), "Courier New", 10, SWT.NORMAL);

  public static final Namespace NAMESPACE_FM = Namespace.getNamespace("fm", "http://www.og.dti.gov/fox_module");

  public static final SAXBuilder DOM_BUILDER;
  public static final XMLOutputter XML_SERIALISER;

  static {

    DOM_BUILDER = new SAXBuilder(new XMLReaderSAX2Factory(false, "net.sf.saxon.aelfred.SAXDriver"));
    DOM_BUILDER.setJDOMFactory(new LocatedJDOMFactory());
    // Bullet proof-ish parser as a FoxModule is not a valid XML due to
    // duplicate namespaces
    DOM_BUILDER.setErrorHandler(new ErrorHandler() {

      @Override
      public void warning(SAXParseException exception) throws SAXException {
        throw exception;
      }

      @Override
      public void fatalError(SAXParseException exception) throws SAXException {
        // Empty
      }

      @Override
      public void error(SAXParseException exception) throws SAXException {
        // Empty
      }
    });

    XML_SERIALISER = new XMLOutputter();
  }
}
