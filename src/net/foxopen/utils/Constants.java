package net.foxopen.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.XMLOutputter;

public class Constants {

  private Constants() {
    // Empty
  }

  public static void init() {
    Logger.logStdout("Init constants");
  }

  public static final Image IMAGE_OK = getImage("/img/actions/ok.png");
  public static final Image IMAGE_MISSING = getImage("/img/actions/no.png");

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

  public static final DocumentBuilderFactory DOM_FACTORY;
  public static final DOMBuilder DOM_BUILDER;
  public static final XMLOutputter XML_SERIALISER;
  public static DocumentBuilder DOC_BUILDER;

  static {
    DOM_FACTORY = DocumentBuilderFactory.newInstance();
    DOM_FACTORY.setNamespaceAware(false);
    DOM_BUILDER = new DOMBuilder();
    try {
      DOC_BUILDER = DOM_FACTORY.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    XML_SERIALISER = new XMLOutputter();
  }
}
