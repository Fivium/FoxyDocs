package net.foxopen.fde.view;

import static net.foxopen.utils.Logger.*;

import java.io.IOException;

import net.foxopen.fde.model.Directory;
import net.foxopen.fde.model.FoxModule;
import net.foxopen.fde.model.abstractObject.AbstractFSItem;
import net.foxopen.fde.model.abstractObject.AbstractModelObject;
import net.foxopen.utils.Loader;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.wb.rcp.databinding.BeansListObservableFactory;
import org.eclipse.wb.rcp.databinding.TreeBeanAdvisor;
import org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

public class FDEMainWindow extends ApplicationWindow {
  private Action action_exit;

  private final static AbstractFSItem root = new Directory(null);
  private Action action_refresh;
  private Action action_open;
  private CTabFolder tabFolder;
  private TreeViewer treeViewerFileList;

  /**
   * Create the application window.
   */
  public FDEMainWindow() {
    super(null);
    createActions();
    addToolBar(SWT.FLAT | SWT.WRAP);
    addMenuBar();
    addStatusLine();
  }

  /**
   * Create contents of the application window.
   * 
   * @param parent
   */
  @Override
  protected Control createContents(Composite parent) {
    setStatus("Welcome to Fox Documentation Editor");
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new FillLayout(SWT.HORIZONTAL));
    {
      SashForm sashForm = new SashForm(container, SWT.NONE);
      sashForm.setSashWidth(4);
      {
        Composite composite = new Composite(sashForm, SWT.NONE);
        composite.setLayout(new TreeColumnLayout());
        {
          treeViewerFileList = new TreeViewer(composite, SWT.BORDER);
          treeViewerFileList.setExpandPreCheckFilters(true);
          treeViewerFileList.setAutoExpandLevel(3);
          treeViewerFileList.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
              IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
              Object selectedNode = thisSelection.getFirstElement();
              if (selectedNode instanceof FoxModule) {
                Tab.open(tabFolder, (FoxModule) selectedNode);
              } else {
                treeViewerFileList.expandToLevel(selectedNode, 1);
              }
            }
          });
          Tree tree = treeViewerFileList.getTree();
        }
      }
      {
        tabFolder = new CTabFolder(sashForm, SWT.BORDER | SWT.CLOSE);
        tabFolder.setSimple(false);
        tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
        // Tab here...
      }
      sashForm.setWeights(new int[] { 1, 3 });
    }
    initDataBindings();

    return container;
  }

  /**
   * Create the actions.
   */
  private void createActions() {
    {
      action_exit = new Action("&Exit") {
        public void run() {
          close();
        }
      };
      action_exit.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/exit.png"));
      action_exit.setAccelerator(SWT.CTRL | 'Q');
    }
    {
      action_refresh = new Action("&Refresh") {
        public void run() {
          if (root.getPath() != null) {
            getStatusLineManager().setMessage("Refresh " + root.getPath());
            try {
              Loader.LoadContent(root);
            } catch (Exception e) {
              MessageBox dialog = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
              dialog.setText("Error");
              dialog.setMessage("Could not refresh the file : " + e.getMessage());
              dialog.open();
            }
          } else {
            MessageBox dialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
            dialog.setText("Warning");
            dialog.setMessage("There is no root folder loaded");
            dialog.open();
          }
        }
      };
      action_refresh.setAccelerator(SWT.F5);
      action_refresh.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/start.png"));
    }
    {
      action_open = new Action("&Open") {
        public void run() {
          // User has selected to save a file
          DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.OPEN);
          String path = dlg.open();
          if (path != null) {
            getStatusLineManager().setMessage("Loading " + path);
            try {
              root.open(path);
              Loader.LoadContent(root);
            } catch (Exception e) {
              logStderr(e.getMessage());
              MessageBox dialog = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
              dialog.setText("Error");
              dialog.setMessage("Could not open the directory : " + e.getMessage());
              dialog.open();
            }
          }
        }
      };
      action_open.setAccelerator(SWT.CTRL | 'O');
      action_open.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/fileopen.png"));
    }
  }

  /**
   * Create the menu manager.
   * 
   * @return the menu manager
   */
  @Override
  protected MenuManager createMenuManager() {
    MenuManager menuManager = new MenuManager("menu");
    {
      MenuManager menu_file = new MenuManager("&File");
      menuManager.add(menu_file);
      menu_file.add(action_open);
      menu_file.add(new Separator());
      menu_file.add(action_exit);
    }
    {
      MenuManager menu_edit = new MenuManager("&Edit");
      menuManager.add(menu_edit);
      menu_edit.add(action_refresh);
    }
    return menuManager;
  }

  /**
   * Create the toolbar manager.
   * 
   * @return the toolbar manager
   */
  @Override
  protected ToolBarManager createToolBarManager(int style) {
    ToolBarManager toolBarManager = new ToolBarManager(SWT.WRAP);
    toolBarManager.add(action_exit);
    toolBarManager.add(action_open);
    toolBarManager.add(action_refresh);
    return toolBarManager;
  }

  /**
   * Create the status line manager.
   * 
   * @return the status line manager
   */
  @Override
  protected StatusLineManager createStatusLineManager() {
    StatusLineManager statusLineManager = new StatusLineManager();
    return statusLineManager;
  }

  /**
   * Launch the application.
   * 
   * @param args
   */
  public static void main(String args[]) {
    logStdout("FDE started");

    // Create the interface
    Display display = Display.getDefault();
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        try {
          final FDEMainWindow window = new FDEMainWindow();
          window.setBlockOnOpen(true);
          window.open();
          Display.getCurrent().dispose();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    logStdout("FDE ended");
  }

  /**
   * Configure the shell.
   * 
   * @param newShell
   */
  @Override
  protected void configureShell(final Shell newShell) {
    newShell.setImage(SWTResourceManager.getImage(FDEMainWindow.class, "/img/actions/Burn.png"));

    newShell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        logStdout("Disposed");
      }

    });
    newShell.addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        logStdout("Closed");

      }
    });

    super.configureShell(newShell);
    newShell.setText("Fox Documentation Editor - Dev 0.2");
  }

  /**
   * Return the initial size of the window.
   */
  @Override
  protected Point getInitialSize() {
    return new Point(205, 215);
  }

  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    //
    BeansListObservableFactory treeObservableFactory = new BeansListObservableFactory(AbstractFSItem.class, "children");
    TreeBeanAdvisor treeAdvisor = new TreeBeanAdvisor(AbstractFSItem.class, "parent", "children", "hasChildren");
    ObservableListTreeContentProvider treeContentProvider = new ObservableListTreeContentProvider(treeObservableFactory, treeAdvisor);
    treeViewerFileList.setLabelProvider(new TreeObservableLabelProvider(treeContentProvider.getKnownElements(), AbstractFSItem.class, "name", "image"));
    treeViewerFileList.setContentProvider(treeContentProvider);
    //
    IObservableList childrenRootObserveList = BeanProperties.list("children").observe(root);
    treeViewerFileList.setInput(childrenRootObserveList);
    //
    return bindingContext;
  }
}
