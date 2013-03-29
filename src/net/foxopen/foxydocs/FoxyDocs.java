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

$Revision$

*/
package net.foxopen.foxydocs;

import static net.foxopen.foxydocs.utils.Logger.logStdout;
import static net.foxopen.foxydocs.view.FoxyDocsMainWindow.getImage;
import net.foxopen.foxydocs.utils.WatchDog;
import net.foxopen.foxydocs.view.FoxyDocsMainWindow;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.jconfig.Configuration;
import org.jconfig.ConfigurationManager;
import org.jconfig.ConfigurationManagerException;
import org.jconfig.handler.XMLFileHandler;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.output.EscapeStrategy;
import org.jdom2.output.Format;
import org.jdom2.output.Format.TextMode;
import org.jdom2.output.XMLOutputter;

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

  public static final Namespace NAMESPACE_FM = Namespace.getNamespace("fm", "http://www.og.dti.gov/fox_module/fm");
  public static final Namespace NAMESPACE_XS = Namespace.getNamespace("xs", "http://www.w3.org/2001/XMLSchema/xs");
  public static final Namespace NAMESPACE_XSI = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance/xsi");

  public static final int EVENT_DOWN = 5402;
  public static final int EVENT_UP = 5403;

  public static final String APP_NAME = "FoyxDocs";
  public static Configuration appConfig;
  private static final XMLFileHandler xmlConfigHandler = new XMLFileHandler("config.xml");
  private static SAXBuilder domBuilder;
  private static XMLOutputter xmlSerialiser;

  // Order matters. Prefix a field with a star to make it mandatory
  public static final String[] ELEMENT_FIELDS = "*description comments pre-condition".split(" "); 
  public static final String[] MODULE_FIELDS = "*name *title *application-title version-desc *description build-notes help-text".split(" ");

  /**
   * Launch the application.
   * 
   * @param args
   * @throws ConfigurationManagerException
   */
  public static void main(String args[]) {
    logStdout("FoxyDocs started");

    // Loading configuration
    try {
      ConfigurationManager.getInstance().load(xmlConfigHandler, APP_NAME);
    } catch (ConfigurationManagerException e1) {
      // Nothing
    }
    appConfig = ConfigurationManager.getConfiguration(APP_NAME);
    appConfig.setLongProperty("lastRun", System.currentTimeMillis());
    saveConfiguration();

    logStdout("Configuration loaded");

    // Creating dom builder
    domBuilder = new SAXBuilder();
    domBuilder.setJDOMFactory(new LocatedJDOMFactory());

    logStdout("DOM Builder created");

    // Create and configure the XML Serialiser
    xmlSerialiser = new XMLOutputter();
    Format prettyPrint = Format.getPrettyFormat();
    prettyPrint.setIndent("  ");
    prettyPrint.setEncoding("UTF-8");
    prettyPrint.setTextMode(TextMode.TRIM_FULL_WHITE);
    prettyPrint.setIgnoreTrAXEscapingPIs(true);
    prettyPrint.setExpandEmptyElements(false);
    prettyPrint.setEscapeStrategy(new EscapeStrategy() {
      @Override
      public boolean shouldEscape(char ch) {
        switch (ch) {
        case '<':
          return true;
        default:
          return false;
        }
      }
    });
    prettyPrint.setSpecifiedAttributesOnly(false);
    xmlSerialiser.setFormat(prettyPrint);

    logStdout("XML Serialiser created");

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
          stopWatchdog();
        }
      }
    });

    logStdout("Ended");
  }

  public static XMLOutputter getSerialiser() {
    return xmlSerialiser;
  }

  public static SAXBuilder getDOMBuilder() {
    return domBuilder;
  }

  /**
   * Stop the watch dog by interrupting the thread
   */
  public static void stopWatchdog() {
    if (FoxyDocs.WATCHDOG != null)
      FoxyDocs.WATCHDOG.interrupt();
  }

  /**
   * Save the configuration.
   */
  public static void saveConfiguration() {
    try {
      ConfigurationManager.getInstance().save(xmlConfigHandler, appConfig);
    } catch (ConfigurationManagerException e) {
      e.printStackTrace();
    }
  }

}
