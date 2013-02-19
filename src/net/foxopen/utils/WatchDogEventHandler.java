package net.foxopen.utils;

import java.nio.file.Path;

public interface WatchDogEventHandler {
  public void modified(Path entryPath);

  public void deleted(Path entryPath);

  public void created(Path parentPath, Path entryPath);
}