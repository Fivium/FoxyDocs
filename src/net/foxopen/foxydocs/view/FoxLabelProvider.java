package net.foxopen.foxydocs.view;

import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider;

public class FoxLabelProvider extends TreeObservableLabelProvider {

  public FoxLabelProvider(IObservableSet allElementsObservable, Class<?> beanClass, String textProperty, String imageProperty) {
    super(allElementsObservable, beanClass, textProperty, imageProperty);
  }

  @Override
  public String getText(Object element) {
    if (element instanceof AbstractModelObject) {
      return getDirtyName((AbstractModelObject) element);
    }
    return super.getText(element);
  }

  public static String getDirtyName(AbstractModelObject object) {
    return object.getName() + (object.isDirty() ? " *" : "");
  }

}
