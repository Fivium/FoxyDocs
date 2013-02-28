package net.foxopen.foxydocs.model.abstractObject;

public interface AbstractDocumentedElement {

  public abstract String getDescription();

  public abstract String getComments();
  
  public abstract String getPrecondition();

}