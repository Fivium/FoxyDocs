package net.foxopen.fde.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.foxopen.utils.Logger;

import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

public class Directory extends AbstractModelObject {

  private List<AbstractModelObject> d_modules = new ArrayList<AbstractModelObject>();

  public Directory(String path) throws IOException, JDOMException, ParserConfigurationException, SAXException {
    getContent(path);
  }

  public Directory() {

  }

  public List<AbstractModelObject> getChildren() {
    return d_modules;
  }

  public void addChild(AbstractModelObject e) {
    d_modules.add(e);
    firePropertyChange("children", null, d_modules);
  }

  public void getContent(String path) throws IOException, JDOMException, ParserConfigurationException, SAXException {
    Logger.logStdout("Opening " + path);
    String files;
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    Logger.logStdout("Items : " + listOfFiles.length);

    for (int i = 0; i < listOfFiles.length; i++) {
      files = listOfFiles[i].getName();
      if (listOfFiles[i].isFile()) {
        if (files.endsWith(".xml") || files.endsWith(".XML")) {
          addChild(new FoxModule(listOfFiles[i].getCanonicalPath()));
        }
      } else if (listOfFiles[i].isDirectory() && !files.startsWith(".svn")) {
        Directory.Load(new Directory(), listOfFiles[i].getCanonicalPath());
      }
    }
  }

  public static void Load(Directory target, String path) {
    Thread t = new Loader(target, path);
    t.start();
  }

  private static class Loader extends Thread {
    private final Directory target;
    final String path;

    public Loader(Directory target, String path) {
      this.target = target;
      this.path = path;
    }

    public void run() {
      try {
        target.getContent(this.path);
      } catch (Exception e) {
        e.printStackTrace();
        Logger.logStderr("Failed to load " + path);
        Logger.logStderr(e.getMessage());
      }
    }
  }

}
