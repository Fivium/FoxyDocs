package net.foxopen.fde.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.foxopen.fde.model.DocumentationEntry;
import net.foxopen.fde.model.FoxModule;
import net.foxopen.fde.model.abstractObject.AbstractModelObject;
import static net.foxopen.utils.Constants.*;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.wb.rcp.databinding.BeansListObservableFactory;
import org.eclipse.wb.rcp.databinding.TreeBeanAdvisor;
import org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider;

public class Tab extends CTabItem {

  private final TreeViewer treeViewer;
  private final StyledText text_documentation;
  private final StyledText text_code;
  private final FoxModule content;

  private Tab(CTabFolder parent, final FoxModule content) {
    super(parent, SWT.CLOSE);
    this.content = content;
    {
      setText(content.getName());
      content.addPropertyChangeListener("name", new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          // Update the tab name, for display the dirty star for instance
          setText(content.getName());
        }
      });
      {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
        composite.setLayout(new FillLayout(SWT.HORIZONTAL));
        {
          SashForm sashFormTabContent = new SashForm(composite, SWT.NONE);

          treeViewer = new TreeViewer(sashFormTabContent, SWT.BORDER);
          treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
          treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
              IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
              Object selectedNode = thisSelection.getFirstElement();
              if (selectedNode instanceof DocumentationEntry) {
                DocumentationEntry entry = (DocumentationEntry) selectedNode;
                // Reset background
                text_code.setLineBackground(0, text_code.getLineCount(), text_code.getBackground());
                // Set background
                text_code.setLineBackground(entry.getLineNumber() - 1, 1, GREY);
                // Scroll to
                text_code.setTopIndex(entry.getLineNumber() - 7);
              } else {
                treeViewer.setExpandedState(selectedNode, !treeViewer.getExpandedState(selectedNode));
              }
            }
          });
          {
            SashForm sashFormCodeDoc = new SashForm(sashFormTabContent, SWT.VERTICAL);
            sashFormCodeDoc.setLayout(new FillLayout(SWT.VERTICAL));

            Group grpDocumentation = new Group(sashFormCodeDoc, SWT.NONE);
            grpDocumentation.setText("Documentation");
            grpDocumentation.setLayout(new FillLayout(SWT.HORIZONTAL));

            text_documentation = new StyledText(grpDocumentation, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
            text_documentation.setFont(FONT_DEFAULT);

            Group grpCode = new Group(sashFormCodeDoc, SWT.NONE);
            grpCode.setText("Code");
            grpCode.setLayout(new FillLayout(SWT.HORIZONTAL));

            text_code = new StyledText(grpCode, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
            text_code.setFont(FONT_DEFAULT);

            // Add the syntax coloring handler
            SyntaxHighlighter.addSyntaxHighligherListener(text_code);

            // Vertical Sash
            sashFormCodeDoc.setWeights(new int[] { 1, 3 });
          }
          // Horizontal Sash
          sashFormTabContent.setWeights(new int[] { 1, 5 });
        }
      }
    }
    DataBindingContext bindingContext = new DataBindingContext();
    //
    BeansListObservableFactory treeObservableFactory = new BeansListObservableFactory(AbstractModelObject.class, "children");
    TreeBeanAdvisor treeAdvisor = new TreeBeanAdvisor(AbstractModelObject.class, "name", "children", null);
    ObservableListTreeContentProvider treeContentProvider = new ObservableListTreeContentProvider(treeObservableFactory, treeAdvisor);
    treeViewer.setLabelProvider(new TreeObservableLabelProvider(treeContentProvider.getKnownElements(), AbstractModelObject.class, "name", "image"));
    treeViewer.setContentProvider(treeContentProvider);
    //
    IObservableList childrenRootObserveList = BeanProperties.list("children").observe(content);
    treeViewer.setInput(childrenRootObserveList);
    //
    IObservableValue observeTextText_documentationObserveWidget = WidgetProperties.text(new int[] { SWT.Modify, SWT.FocusOut, SWT.DefaultSelection }).observe(text_documentation);
    IObservableValue observeSingleSelectionTreeViewer = ViewerProperties.singleSelection().observe(treeViewer);
    IObservableValue treeViewerDocumentationObserveDetailValue = BeanProperties.value(AbstractModelObject.class, "documentation", String.class).observeDetail(observeSingleSelectionTreeViewer);
    bindingContext.bindValue(observeTextText_documentationObserveWidget, treeViewerDocumentationObserveDetailValue, null, null);
    //
    IObservableValue observeTextText_documentationObserveWidget2 = WidgetProperties.text(new int[] { SWT.Modify, SWT.FocusOut, SWT.DefaultSelection }).observe(text_code);
    IObservableValue observeSingleSelectionTreeViewer2 = ViewerProperties.singleSelection().observe(treeViewer);
    IObservableValue treeViewerDocumentationObserveDetailValue2 = BeanProperties.value(AbstractModelObject.class, "code", String.class).observeDetail(observeSingleSelectionTreeViewer2);
    bindingContext.bindValue(observeTextText_documentationObserveWidget2, treeViewerDocumentationObserveDetailValue2, null, null);
  }

  /**
   * Is the content of the tab equals that FoxModule ?
   * 
   * @param that
   *          The FoxModule to compare
   * @return True if the content equals that, False otherwise
   */
  public boolean equals(FoxModule that) {
    return content.equals(that);
  }

  /**
   * Open a new tab or set the focus to a open one
   * 
   * @param parent
   *          The parent Tab Folder
   * @param content
   *          The FoxModule to open in a tab
   */
  public static void open(CTabFolder parent, FoxModule content) {
    // Check if the tab is already open. If so, set selection to it. If not,
    // open a new one.
    Tab tab = getOpenedTab(parent, content);
    if (tab == null) {
      tab = new Tab(parent, content);
    }

    // Set Focus
    parent.setSelection(tab);

    // Open code
    tab.text_code.setText(content.getCode());
  }

  /**
   * Return an open tab with the selected content
   * 
   * @param folder
   *          The parent Tab Folder
   * @param content
   *          The content module
   * @return The tab, or null if there is no open tab with this content
   */
  public static Tab getOpenedTab(CTabFolder folder, FoxModule content) {
    for (CTabItem f : folder.getItems()) {
      if (f instanceof Tab && ((Tab) f).equals(content))
        return (Tab) f;
    }
    return null;
  }

}
