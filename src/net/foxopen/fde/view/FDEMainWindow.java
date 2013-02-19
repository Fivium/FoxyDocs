package net.foxopen.fde.view;

import static net.foxopen.utils.Logger.logStdout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import net.foxopen.fde.model.Directory;
import net.foxopen.fde.model.FoxModule;
import net.foxopen.fde.model.abstractObject.AbstractFSItem;
import net.foxopen.utils.FoxyDocs;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.rcp.databinding.BeansListObservableFactory;
import org.eclipse.wb.rcp.databinding.TreeBeanAdvisor;
import org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

public class FDEMainWindow extends ApplicationWindow {
  private Action action_exit;

  private static AbstractFSItem root;
  private TreeViewer treeViewerFileList;
  private CTabFolder tabFolder;
  private Action action_open;
  private Action action_close;
  private Action action_about;
  private Action action_nextentry;
  private Action action_previousentry;
  private Action action_nextfile;
  private Action action_previousfile;
  private Action action_save;

  static {
    try {
      root = new Directory(null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

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

  public static AbstractFSItem getRoot() {
    return root;
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
                treeViewerFileList.setExpandedState(selectedNode, !treeViewerFileList.getExpandedState(selectedNode));
              }
            }
          });
          // Tree tree = treeViewerFileList.getTree();
        }
      }
      {
        tabFolder = new CTabFolder(sashForm, SWT.BORDER | SWT.CLOSE);
        tabFolder.setSimple(false);
        tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
        // Tab here...
      }
      sashForm.setWeights(new int[] { 1, 5 });
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
      action_open = new Action("&Open...") {
        public void run() {
          // User has selected to save a file
          DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.OPEN);
          String path = dlg.open();
          if (path != null) {
            try {
              root.open(path);
              new ProgressMonitorDialog(getShell()).run(false, true, Loader.LoadContent(root));
            } catch (InvocationTargetException e) {
              e.printStackTrace();
              MessageDialog.openError(getShell(), "Error", e.getMessage());
            } catch (InterruptedException e) {
              e.printStackTrace();
              MessageDialog.openInformation(getShell(), "Cancelled", e.getMessage());
            } catch (Exception e) {
              e.printStackTrace();
              MessageDialog.openInformation(getShell(), "Error", e.getMessage());
            }
          }
        }
      };
      action_open.setAccelerator(SWT.CTRL | 'O');
      action_open.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/folder_new.png"));
    }
    {
      action_close = new Action("&Close Tab") {
        public void run() {
          if (tabFolder.getSelection() != null) {
            tabFolder.getSelection().dispose();
          }
        }
      };
      action_close.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/cancel.png"));
      action_close.setAccelerator(SWT.CTRL | 'W');
    }
    {
      action_about = new Action("&About...") {
        public void run() {
          MessageDialog.openInformation(getShell(), "About", "A Fox Documentation Editor\n\npierredominique.putallaz@fivium.co.uk\n\nhttps://github.com/Akkenar/FoxyDocs");
        }

      };
      action_about.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/about_kde.png"));
    }
    {
      action_nextentry = new Action("Next Entry") {
        public void run() {
          if (tabFolder.getSelection() != null) {
            tabFolder.getSelection().notifyListeners(FoxyDocs.EVENT_DOWN, new Event());
          }
        }

      };
      action_nextentry.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/adept_reinstall.png"));
      action_nextentry.setAccelerator(SWT.ALT | 'S');
    }
    {
      action_previousentry = new Action("Previous Entry") {
        public void run() {
          if (tabFolder.getSelection() != null) {
            tabFolder.getSelection().notifyListeners(FoxyDocs.EVENT_UP, new Event());
          }
        }
      };
      action_previousentry.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/up.png"));
      action_previousentry.setAccelerator(SWT.ALT | 'W');
    }
    {
      action_nextfile = new Action("Next File") {

      };
      action_nextfile.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/finish.png"));
      action_nextfile.setAccelerator(SWT.ALT | 'D');
    }
    {
      action_previousfile = new Action("Previous File") {

      };
      action_previousfile.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/start.png"));
      action_previousfile.setAccelerator(SWT.ALT | 'A');
    }
    {
      action_save = new Action("&Save") {
        public void run() {
          try {
            root.save();
          } catch (Exception e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", e.getMessage());
          }
        }
      };
      action_save.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/save_all.png"));
      action_save.setAccelerator(SWT.CTRL | 'S');
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
      menu_file.add(action_save);
      menu_file.add(new Separator());
      menu_file.add(action_exit);
    }
    {
      MenuManager menu_edit = new MenuManager("&Edit");
      menuManager.add(menu_edit);
    }

    MenuManager menu_entries = new MenuManager("&Browse");
    menuManager.add(menu_entries);
    menu_entries.add(action_previousentry);
    menu_entries.add(action_nextentry);
    menu_entries.add(new Separator());
    menu_entries.add(action_previousfile);
    menu_entries.add(action_nextfile);
    menu_entries.add(new Separator());
    menu_entries.add(action_close);

    MenuManager menu_help = new MenuManager("&Help");
    menuManager.add(menu_help);
    menu_help.add(action_about);
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
    toolBarManager.add(action_open);
    toolBarManager.add(action_save);
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
    // Initialise statics constants
    FoxyDocs.init();
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
        } finally {
          // Kill the watch dog thread
          if (FoxyDocs.WATCHDOG != null)
            FoxyDocs.WATCHDOG.interrupt();
        }
      }
    });

    logStdout("Ended");
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
    newShell.setText("Fox Documentation Editor - Dev 0.3");
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
