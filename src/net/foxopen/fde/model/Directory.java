package net.foxopen.fde.model;

import static net.foxopen.utils.Logger.logStdout;

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

  public Directory(Directory parent) {
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
    cascadeFirePropertyChange("children", null, getChildren());
  }

  public boolean getFirstLevel() {
    return true;
  }

  private List<FoxModule> loadFoxModuleList(String path) throws IOException, JDOMException, ParserConfigurationException, SAXException {
    Logger.logStdout("Opening " + path);
    String files;
    File folder = new File(path);
    if (folder.isFile()) {
      throw new IllegalArgumentException("You cannot load just one file : " + path);
    }
    name = folder.getName();
    File[] listOfFiles = folder.listFiles();
    Logger.logStdout("Items : " + listOfFiles.length);

    // Reset or init the content list
    contentList = new ArrayList<AbstractModelObject>();
    cascadeFirePropertyChange("children", null, getChildren());

    // Populate stuff
    for (int i = 0; i < listOfFiles.length; i++) {
      files = listOfFiles[i].getName();
      if (listOfFiles[i].isFile()) {
        if (files.endsWith(".xml") || files.endsWith(".XML")) {
          addChild(new FoxModule(listOfFiles[i], this));
        }
      } else if (listOfFiles[i].isDirectory() && !files.startsWith(".svn")) {
        Directory d = new Directory(this);
        d.loadFoxModuleList(listOfFiles[i].getCanonicalPath());
        addChild(d);
      }
    }

    return getFoxModules();
  }

  public List<FoxModule> getFoxModules() {
    List<FoxModule> buffer = new ArrayList<FoxModule>();
    for (AbstractModelObject e : getChildren()) {
      if (e instanceof FoxModule) {
        buffer.add((FoxModule) e);
      } else if (e instanceof Directory) {
        buffer.addAll(((Directory) e).getFoxModules());
      }
    }
    return buffer;
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
        List<FoxModule> modules = target.loadFoxModuleList(target.getPath());
        logStdout(modules.size() + " Fox Modules");
        for(FoxModule f:modules) {
          f.read();
        }
      } catch (Exception e) {
        e.printStackTrace();
        Logger.logStderr("Failed to load " + target.getPath());
        Logger.logStderr(e.getMessage());
      }
    }
  }

}
