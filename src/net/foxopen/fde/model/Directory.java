package net.foxopen.fde.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.foxopen.fde.model.FoxModule.NotAFoxModuleException;
import net.foxopen.utils.Logger;

import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

public class Directory extends AbstractModelObject {

  private List<AbstractModelObject> contentList;
  private String name;
  private String path;

  public Directory() {
    parent = null;
  }

  private Directory(String path, Directory parent) throws IOException, JDOMException, ParserConfigurationException, SAXException {
    getContent(path);
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<AbstractModelObject> getChildren() {
    return contentList;
  }

  public void addChild(AbstractModelObject e) {
    contentList.add(e);
    cascadeFirePropertyChange("children", null, contentList);
  }
  
  public boolean getFirstLevel(){
    return true;
  }

  private void getContent(String path) throws IOException, JDOMException, ParserConfigurationException, SAXException {
    Logger.logStdout("Opening " + path);
    String files;
    File folder = new File(path);
    if (folder.isFile()) {
      throw new IllegalArgumentException("You cannot load just one file : " + path);
    }
    name = folder.getName();
    File[] listOfFiles = folder.listFiles();
    Logger.logStdout("Items : " + listOfFiles.length);

    // Reset the content list
    contentList = new ArrayList<AbstractModelObject>();
    firePropertyChange("children", null, null);

    // Populate stuff
    for (int i = 0; i < listOfFiles.length; i++) {
      files = listOfFiles[i].getName();
      if (listOfFiles[i].isFile()) {
        if (files.endsWith(".xml") || files.endsWith(".XML")) {
          try {
            addChild(new FoxModule(listOfFiles[i].getCanonicalPath(), this));
          } catch (NotAFoxModuleException e) {
            Logger.logStderr(e.getMessage());
          }
        }
      } else if (listOfFiles[i].isDirectory() && !files.startsWith(".svn")) {
        addChild(new Directory(listOfFiles[i].getCanonicalPath(),this));
      }
    }
  }

  public static void Load(Directory target, String path) {
    target.setPath(path);
    Load(target);
  }

  public static void Load(Directory target) {
    Thread t = new Loader(target);
    t.start();
  }

  private static class Loader extends Thread {
    private final Directory target;

    public Loader(Directory target) {
      this.target = target;
      if (target.getPath() == null) {
        throw new IllegalArgumentException("You must set a path for this Directory");
      }
    }

    public void run() {
      try {
        target.getContent(target.getPath());
      } catch (Exception e) {
        e.printStackTrace();
        Logger.logStderr("Failed to load " + target.getPath());
        Logger.logStderr(e.getMessage());
      }
    }
  }

}
