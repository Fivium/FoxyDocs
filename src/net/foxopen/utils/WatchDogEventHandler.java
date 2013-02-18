package net.foxopen.utils;

import net.foxopen.fde.model.abstractObject.AbstractFSItem;
import net.foxopen.fde.model.abstractObject.AbstractModelObject;

public interface WatchDogEventHandler {
  public void modified(AbstractFSItem item);

  public void deleted(AbstractFSItem entry);

  public void created(AbstractModelObject parent, AbstractFSItem entry);
}