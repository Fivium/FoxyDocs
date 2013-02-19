package net.foxopen.utils;

import java.nio.file.Path;

import net.foxopen.foxydocs.model.abstractObject.AbstractFSItem;

public interface WatchDogEventHandler {
  public void modified(AbstractFSItem item);

  public void deleted(AbstractFSItem entry);

  public void created(AbstractFSItem parent, Path entry);
}