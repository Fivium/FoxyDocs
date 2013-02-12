package net.foxopen.fde.view;

import static net.foxopen.utils.Logger.logStdout;
import net.foxopen.fde.model.AbstractModelObject;
import net.foxopen.fde.model.Directory;
import net.foxopen.fde.model.FoxModule;

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
import org.eclipse.swt.custom.StyledText;
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

public class FDEMainWindow extends ApplicationWindow {
  private Action action_exit;

  // private static List<AbstractModelObject> d_modules = new
  // ArrayList<AbstractModelObject>();
  private final static Directory root = new Directory();;
  private Action action_refresh;
  private Action action_open;
  private CTabFolder tabFolder;
  private TreeViewer treeViewer_1;

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
          treeViewer_1 = new TreeViewer(composite, SWT.BORDER);
          treeViewer_1.setExpandPreCheckFilters(true);
          treeViewer_1.setAutoExpandLevel(3);
          treeViewer_1.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
              IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
              Object selectedNode = thisSelection.getFirstElement();
              if (selectedNode instanceof FoxModule) {
                new Tab(tabFolder, (FoxModule) selectedNode);
              }
            }
          });
          Tree tree = treeViewer_1.getTree();
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
            Directory.Load(root);
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
            Directory.Load(root, path);
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
    toolBarManager.add(action_open);
    toolBarManager.add(action_exit);
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
    BeansListObservableFactory treeObservableFactory = new BeansListObservableFactory(AbstractModelObject.class, "children");
    TreeBeanAdvisor treeAdvisor = new TreeBeanAdvisor(AbstractModelObject.class, "name", "children", null);
    ObservableListTreeContentProvider treeContentProvider = new ObservableListTreeContentProvider(treeObservableFactory, treeAdvisor);
    treeViewer_1.setLabelProvider(new TreeObservableLabelProvider(treeContentProvider.getKnownElements(), AbstractModelObject.class, "name", null));
    treeViewer_1.setContentProvider(treeContentProvider);
    //
    IObservableList childrenRootObserveList = BeanProperties.list("children").observe(root);
    treeViewer_1.setInput(childrenRootObserveList);

    return bindingContext;
  }
}
