package net.foxopen.fde.view;

import static net.foxopen.utils.Constants.BLUE;
import static net.foxopen.utils.Constants.CYAN;
import static net.foxopen.utils.Constants.GREEN;
import static net.foxopen.utils.Constants.RED;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import net.foxopen.fde.model.FoxModule;
import net.foxopen.fde.model.abstractObject.AbstractModelObject;
import net.xmlparser.XmlRegion;
import net.xmlparser.XmlRegionAnalyzer;

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
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
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
              treeViewer.setExpandedState(selectedNode, !treeViewer.getExpandedState(selectedNode));
            }
          });
          {
            SashForm sashFormCodeDoc = new SashForm(sashFormTabContent, SWT.VERTICAL);
            sashFormCodeDoc.setLayout(new FillLayout(SWT.VERTICAL));

            Group grpDocumentation = new Group(sashFormCodeDoc, SWT.NONE);
            grpDocumentation.setText("Documentation");
            grpDocumentation.setLayout(new FillLayout(SWT.HORIZONTAL));

            text_documentation = new StyledText(grpDocumentation, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

            Group grpCode = new Group(sashFormCodeDoc, SWT.NONE);
            grpCode.setText("Code");
            grpCode.setLayout(new FillLayout(SWT.HORIZONTAL));

            text_code = new StyledText(grpCode, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
           

            // Add the syntax coloring handler
            text_code.addExtendedModifyListener(new ExtendedModifyListener() {
              public void modifyText(ExtendedModifyEvent event) {
                int end = event.start + event.length - 1;
                String text = text_code.getText(event.start, end);

                XmlRegionAnalyzer analyzer = new XmlRegionAnalyzer();
                List<XmlRegion> regions = analyzer.analyzeXml(text);
                for (XmlRegion xr : regions) {
                  // Create a collection to hold the StyleRanges
                  ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();

                  int regionLength = xr.getEnd() - xr.getStart();
                  switch (xr.getXmlRegionType()) {
                  case MARKUP:
                    ranges.add(new StyleRange(xr.getStart(), regionLength, RED, null));
                    break;
                  case ATTRIBUTE:
                    ranges.add(new StyleRange(xr.getStart(), regionLength, RED, null, SWT.BOLD));
                    break;
                  case ATTRIBUTE_VALUE:
                    ranges.add(new StyleRange(xr.getStart(), regionLength, GREEN, null, SWT.BOLD));
                    break;
                  case MARKUP_VALUE:
                    ranges.add(new StyleRange(xr.getStart(), regionLength, GREEN, null));
                    break;
                  case COMMENT:
                    ranges.add(new StyleRange(xr.getStart(), regionLength, BLUE, null));
                    break;
                  case INSTRUCTION:
                    ranges.add(new StyleRange(xr.getStart(), regionLength, CYAN, null));
                    break;
                  case CDATA:
                    ranges.add(new StyleRange(xr.getStart(), regionLength, BLUE, null, SWT.BOLD));
                    break;
                  case WHITESPACE:
                    break;
                  default:
                    break;
                  }

                  // If we have any ranges to set, set them
                  if (!ranges.isEmpty()) {
                    text_code.replaceStyleRanges(xr.getStart(), regionLength, (StyleRange[]) ranges.toArray(new StyleRange[0]));
                  }
                }

              }
            });

            sashFormCodeDoc.setWeights(new int[] { 1, 2 });
          }
          sashFormTabContent.setWeights(new int[] { 1, 3 });
        }
      }
    }
    // Add Top Content
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

  public boolean equals(FoxModule that) {
    return content.equals(that);
  }

  public static void open(CTabFolder parent, FoxModule selectedNode) {
    Tab tab = exists(parent, selectedNode);

    if (tab == null) {
      tab = new Tab(parent, selectedNode);
    }

    // Set Focus
    parent.setSelection(tab);
  }

  public static Tab exists(CTabFolder folder, FoxModule selectedNode) {
    for (CTabItem f : folder.getItems()) {
      if (f instanceof Tab && ((Tab) f).equals(selectedNode))
        return (Tab) f;
    }
    return null;
  }

}
