package net.foxopen.utils;

import net.foxopen.fde.view.FDEMainWindow;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.ResourceManager;

public class Constants {
 
    private Constants(){
      
    }
    
    public static final Image IMAGE_OK = getImage("/img/actions/ok.png");
    public static final Image IMAGE_MISSING = getImage("/img/actions/no.png");

    private static Image getImage(String file) {
      ImageDescriptor image = ResourceManager.getImageDescriptor(FDEMainWindow.class, file);
      return image.createImage();
    }
}
