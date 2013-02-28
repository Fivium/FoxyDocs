package net.foxopen.foxydocs.model.abstractObject;

public interface AbstractDocumentedElement {

  public abstract String getDescription();

  public abstract String getComments();
  
  public abstract String getPrecondition();
  
  public abstract void setDescription(String c);

  public abstract void setComments(String c);
  
  public abstract void setPrecondition(String c);

}