/*
Copyright (c) 2013, ENERGY DEVELOPMENT UNIT (INFORMATION TECHNOLOGY)
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
 * Neither the name of the DEPARTMENT OF ENERGY AND CLIMATE CHANGE nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package net.foxopen.foxydocs.view;

import static net.foxopen.foxydocs.FoxyDocs.EVENT_DOWN;
import static net.foxopen.foxydocs.FoxyDocs.EVENT_UP;
import static net.foxopen.foxydocs.FoxyDocs.FONT_DEFAULT;
import static net.foxopen.foxydocs.FoxyDocs.GREY;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.foxopen.foxydocs.model.DocumentedElement;
import net.foxopen.foxydocs.model.DocumentedElementSet;
import net.foxopen.foxydocs.model.FoxModule;
import net.foxopen.foxydocs.model.abstractObject.AbstractModelObject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.rcp.databinding.BeansListObservableFactory;
import org.eclipse.wb.rcp.databinding.TreeBeanAdvisor;
import org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider;

import org.eclipse.swt.widgets.Label;
import org.jdom2.Element;

public class Tab extends CTabItem {

  private final TreeViewer treeViewer;
  private final HashMap<String, StyledText> docElements = new HashMap<String, StyledText>();

  private final StyledText codeText;
  private final FoxModule content;
  private final ArrayList<DocumentedElement> docEntries;
  private final CTabItem regularDocTab;
  private final CTabItem headerDocTab;
  private final Composite moduleInfoDocComposite;
  private final DataBindingContext bindingContext = new DataBindingContext();

  /**
   * A new tab with dynamic content
   * 
   * @param parent
   *          The CTabFolder to add to
   * @param content
   *          The FoxModule displayed in the tab
   */
  private Tab(CTabFolder parent, final FoxModule content) {
    super(parent, SWT.CLOSE);
    if (content == null) {
      throw new IllegalArgumentException("The tab content must not be null");
    }
    this.content = content;
    this.docEntries = content.getAllEntries();
    {
      setText(content.getName());
      content.addPropertyChangeListener("name", new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          // Update the tab name, for display the dirty star for instance
          if (!isDisposed())
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
          treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
          treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
              IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
              Object selectedNode = thisSelection.getFirstElement();
              if (selectedNode instanceof DocumentedElementSet) {
                treeViewer.setExpandedState(selectedNode, !treeViewer.getExpandedState(selectedNode));
              }
            }
          });
          // An entry is selected
          treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
              IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
              Object selectedNode = thisSelection.getFirstElement();
              goToCode(selectedNode);

              // Set focus and editable
              if (selectedNode instanceof DocumentedElement) {
                regularDocTab.getParent().setSelection(regularDocTab);
                docElements.get("description").setFocus();
              } else {
                // TODO Do something if a DocumentElementSet is selected
              }
            }
          });
          {
            SashForm sashFormCodeDoc = new SashForm(sashFormTabContent, SWT.VERTICAL);
            sashFormCodeDoc.setLayout(new FillLayout(SWT.VERTICAL));

            CTabFolder tabFolderDoc = new CTabFolder(sashFormCodeDoc, SWT.BORDER | SWT.NONE);
            tabFolderDoc.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

            regularDocTab = new CTabItem(tabFolderDoc, SWT.NONE);
            regularDocTab.setText("Documentation");
            tabFolderDoc.setSelection(regularDocTab); // Default tab

            // Regular documentation composite
            Composite regularDocComposite = new Composite(tabFolderDoc, SWT.NONE);
            regularDocComposite.setLayout(new GridLayout(2, false));

            // Fields
            addElement(regularDocComposite, "description");
            addElement(regularDocComposite, "comments");
            addElement(regularDocComposite, "precondition");

            regularDocTab.setControl(regularDocComposite);

            headerDocTab = new CTabItem(tabFolderDoc, SWT.NONE);
            headerDocTab.setText("Module Informations");

            // Module Info Composite
            ScrolledComposite moduleInfoWrapper = new ScrolledComposite(tabFolderDoc, SWT.BORDER | SWT.V_SCROLL);
            moduleInfoWrapper.setExpandHorizontal(true);
            moduleInfoWrapper.setExpandVertical(false);

            moduleInfoDocComposite = new Composite(moduleInfoWrapper, SWT.BORDER);
            moduleInfoDocComposite.setLayout(new GridLayout(2, false));
            for (Entry<String, Element> e : content.getModuleInfo().getContent()) {
              addStaticElement(moduleInfoDocComposite, e);
            }

            moduleInfoDocComposite.pack(true);
            moduleInfoWrapper.setContent(moduleInfoDocComposite);
            moduleInfoWrapper.setMinSize(moduleInfoDocComposite.computeSize(SWT.NONE, SWT.DEFAULT));

            headerDocTab.setControl(moduleInfoWrapper);

            Group grpCode = new Group(sashFormCodeDoc, SWT.NONE);
            grpCode.setText("Code");
            grpCode.setLayout(new FillLayout(SWT.HORIZONTAL));

            codeText = new StyledText(grpCode, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
            codeText.setFont(FONT_DEFAULT);

            // Add the syntax colouring handler
            SyntaxHighlighter.addSyntaxHighligherListener(codeText);

            // Browse Listener
            this.addListener(EVENT_DOWN, new Listener() {
              @Override
              public void handleEvent(Event event) {
                IStructuredSelection thisSelection = (IStructuredSelection) treeViewer.getSelection();
                Object selectedNode = thisSelection.getFirstElement();
                if (treeViewer.getSelection() == null) {
                  treeViewer.setSelection(new StructuredSelection(docEntries.get(0)), true);
                } else {
                  int index = Math.min(docEntries.size() - 1, docEntries.indexOf(selectedNode) + 1);
                  treeViewer.setSelection(new StructuredSelection(docEntries.get(index)), true);
                }
              }
            });

            this.addListener(EVENT_UP, new Listener() {
              @Override
              public void handleEvent(Event event) {
                IStructuredSelection thisSelection = (IStructuredSelection) treeViewer.getSelection();
                Object selectedNode = thisSelection.getFirstElement();
                if (treeViewer.getSelection() == null) {
                  treeViewer.setSelection(new StructuredSelection(docEntries.get(0)), true);
                } else {
                  int index = Math.max(0, docEntries.indexOf(selectedNode) - 1);
                  treeViewer.setSelection(new StructuredSelection(docEntries.get(index)), true);
                }
              }
            });

            // Update code
            content.addPropertyChangeListener("code", new PropertyChangeListener() {
              @Override
              public void propertyChange(PropertyChangeEvent event) {
                int topIndex = codeText.getTopIndex();
                codeText.setText(content.getCode());
                codeText.setTopIndex(topIndex);
                goToCode(content);
              }
            });

            // Vertical Sash
            sashFormCodeDoc.setSashWidth(4);
            sashFormCodeDoc.setWeights(new int[] { 1, 2 });
          }
          // Horizontal Sash
          sashFormTabContent.setSashWidth(4);
          sashFormTabContent.setWeights(new int[] { 1, 5 });
        }
      }
    }
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

  }

  /**
   * Add a dynamic element Label|Styled Text and bind it to the model
   * 
   * @param parent
   *          The parent to add to
   * @param key
   *          The name of the variable to bind
   */
  private void addElement(Composite parent, String key) {
    if (!(parent.getLayout() instanceof GridLayout)) {
      throw new IllegalArgumentException("The parent composite must have a Grid Layout");
    }
    if (key == null || key.trim().isEmpty()) {
      throw new IllegalArgumentException("The key must not be null or empty");
    }

    // Label with first upper letter
    Label tLabel = new Label(parent, SWT.HORIZONTAL);
    tLabel.setText(key.substring(0, 1).toUpperCase() + key.substring(1));
    tLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));

    // Editable text
    StyledText tText = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
    tText.setFont(FONT_DEFAULT);
    tText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
    docElements.put(key, tText);

    bind(tText, key);
  }

  /**
   * Add a static element Label|Styled Text
   * 
   * @param parent
   *          The parent to add to
   * @param entry
   *          The variable to display
   * @return The Styled Text created
   */
  private void addStaticElement(Composite parent, final Entry<String, Element> entry) {
    if (!(parent.getLayout() instanceof GridLayout)) {
      throw new IllegalArgumentException("The parent composite must have a Grid Layout");
    }
    if (entry == null || entry.getKey().trim().isEmpty()) {
      throw new IllegalArgumentException("The key must not be null or empty");
    }

    // Label with first upper letter
    Label tLabel = new Label(parent, SWT.HORIZONTAL);
    tLabel.setText(entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1));
    tLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 3));

    // Editable text
    final StyledText tText = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
    tText.setFont(FONT_DEFAULT);
    GridData layout = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 3);
    layout.heightHint = 50;
    tText.setLayoutData(layout);
    docElements.put(entry.getKey(), tText);

    // Manual binding
    tText.setText(entry.getValue().getTextNormalize());
    tText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        content.getModuleInfo().change(entry.getKey(),tText.getText());
     }
    });
  }

  /**
   * Bind a styled text to the treeViewer
   * 
   * @param pStyledText
   *          The styled text to bind
   * @param pKey
   *          the name of the variable in the model to bind to
   */
  private void bind(StyledText pStyledText, String pKey) {
    // Add listener
    IObservableValue observeTextText_documentationObserveWidget = WidgetProperties.text(new int[] { SWT.Modify, SWT.FocusOut, SWT.DefaultSelection }).observe(pStyledText);
    IObservableValue observeSingleSelectionTreeViewer = ViewerProperties.singleSelection().observe(treeViewer);
    IObservableValue treeViewerDocumentationObserveDetailValue = BeanProperties.value(DocumentedElement.class, pKey, String.class).observeDetail(observeSingleSelectionTreeViewer);
    bindingContext.bindValue(observeTextText_documentationObserveWidget, treeViewerDocumentationObserveDetailValue, null, null);
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
   * Scroll down the code to the selected node and highlight the first line
   * 
   * @param selectedNode
   *          The node to select
   */
  private void goToCode(Object selectedNode) {
    if (selectedNode instanceof DocumentedElement) {
      DocumentedElement entry = (DocumentedElement) selectedNode;
      // Reset background
      codeText.setLineBackground(0, codeText.getLineCount(), codeText.getBackground());
      // Set background
      codeText.setLineBackground(entry.getLineNumber() - 1, 1, GREY);
      // Scroll to
      codeText.setTopIndex(entry.getLineNumber() - 7);
    }
  }

  /**
   * Open a new tab or set the focus to a open one
   * 
   * @param parent
   *          The parent Tab Folder
   * @param content
   *          The FoxModule to open in a tab
   * @throws IOException
   */
  public static void open(CTabFolder parent, FoxModule content) throws IOException {
    // Check if the tab is already open. If so, set selection to it. If not,
    // open a new one.
    Tab tab = getOpenedTab(parent, content);
    if (tab == null) {
      tab = new Tab(parent, content);
    }

    // Set Focus
    parent.setSelection(tab);

    // Open code
    tab.codeText.setText(content.getCode());

    // Open the first entry
    if (tab.docEntries.size() > 1)
      tab.treeViewer.setSelection(new StructuredSelection(tab.docEntries.get(0)), true);
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
