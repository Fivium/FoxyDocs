package net.foxopen.utils;

import net.foxopen.fde.view.FDEMainWindow;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.ResourceManager;

public class Constants {
 
    private Constants(){
      // Empty
    }
    
    public static final Image IMAGE_OK = getImage("/img/actions/ok.png");
    public static final Image IMAGE_MISSING = getImage("/img/actions/no.png");

    private static Image getImage(String file) {
      ImageDescriptor image = ResourceManager.getImageDescriptor(FDEMainWindow.class, file);
      return image.createImage();
    }
    
    private static final Device device = Display.getCurrent();
    public static final Color RED = new Color(device, 255, 0, 0);
    public static final Color GREEN = new Color(device, 0, 255, 0);
    public static final Color CYAN = new Color(device, 0, 255, 255);
    public static final Color BLUE = new Color(device, 0, 0, 255);
    public static final Color PURPLE = new Color(device, 255, 0, 255);
    public static final Color YELLOW = new Color(device, 255, 255, 0);
}
