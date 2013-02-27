/*
Copyright (c) 2013, ENERGY DEVELOPMENT UNIT (INFORMATION TECHNOLOGY)
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the DEPARTMENT OF ENERGY AND CLIMATE CHANGE nor the
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
package net.foxopen.foxydocs;

import static net.foxopen.foxydocs.utils.Logger.logStdout;
import net.foxopen.foxydocs.utils.WatchDog;
import net.foxopen.foxydocs.view.FoxyDocsMainWindow;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.ResourceManager;
import org.jconfig.Configuration;
import org.jconfig.ConfigurationManager;
import org.jconfig.ConfigurationManagerException;
import org.jconfig.handler.XMLFileHandler;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FoxyDocs {

  public static WatchDog WATCHDOG;
  
  public final static String FOX_MODULE_XPATH = "/xs:schema/xs:annotation/xs:appinfo/fm:module/";

  public final static int STATUS_UNKNOWN = 0x0;
  public final static int STATUS_MISSING = 0x01;
  public final static int STATUS_OK = 0x02;
  public final static int STATUS_PARTIAL = 0x03;

  public static final Image IMAGE_OK = getImage("/img/actions/ok.png");
  public static final Image IMAGE_MISSING = getImage("/img/actions/cancel.png");
  public static final Image IMAGE_PARTIAL = getImage("/img/actions/edit_remove.png");
  public static final Image IMAGE_UNKNOWN = getImage("/img/actions/messagebox_question.png");

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
  public static final Namespace NAMESPACE_XS = Namespace.getNamespace("xs", "http://www.w3.org/2001/XMLSchema");

  public static SAXBuilder DOM_BUILDER;
  public static XMLOutputter XML_SERIALISER;

  public static final int EVENT_DOWN = 5402;
  public static final int EVENT_UP = 5403;
  public static final int EVENT_NEXT = 5404;
  public static final int EVENT_PREVIOUS = 5405;

  public static final String APP_NAME = "FoyxDocs";
  public static Configuration appConfig;
  private static final XMLFileHandler xmlConfigHandler = new XMLFileHandler("config.xml");

  /**
   * Launch the application.
   * 
   * @param args
   * @throws ConfigurationManagerException
   */
  public static void main(String args[]) {
    logStdout("FDE started");

    // Loading configuration
    try {
      ConfigurationManager.getInstance().load(xmlConfigHandler, APP_NAME);
    } catch (ConfigurationManagerException e1) {
     // Nothing
    }
    appConfig = ConfigurationManager.getConfiguration(APP_NAME);
    appConfig.setLongProperty("lastRun", System.currentTimeMillis());
    saveConfiguration();

    // Creating dom builder
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
        // TODO
      }

      @Override
      public void error(SAXParseException exception) throws SAXException {
        // Empty
        // TODO
      }
    });

    // Create and configure the XML Serialiser
    XML_SERIALISER = new XMLOutputter();
    Format prettyPrint = Format.getPrettyFormat();
    prettyPrint.setIndent("  ");
    prettyPrint.setEncoding("UTF-8");
    XML_SERIALISER.setFormat(prettyPrint);

    // Create the interface
    Display display = Display.getDefault();
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      @Override
      public void run() {
        try {
          final FoxyDocsMainWindow window = new FoxyDocsMainWindow();
          window.setBlockOnOpen(true);
          window.open();
          Display.getCurrent().dispose();
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          // Kill the watch dog thread
          if (FoxyDocs.WATCHDOG != null)
            FoxyDocs.WATCHDOG.interrupt();
        }
      }
    });

    logStdout("Ended");
  }

  private static Image getImage(String file) {
    ImageDescriptor image = ResourceManager.getImageDescriptor(FoxyDocsMainWindow.class, file);
    return image.createImage();
  }

  public static void saveConfiguration(){
    try {
      ConfigurationManager.getInstance().save(xmlConfigHandler, appConfig);
    } catch (ConfigurationManagerException e) {
      e.printStackTrace();
    }
  }

}
