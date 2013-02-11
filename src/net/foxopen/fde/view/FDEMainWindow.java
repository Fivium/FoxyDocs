package net.foxopen.fde.view;

import static net.foxopen.utils.Logger.logStdout;

import java.util.ArrayList;
import java.util.List;

import net.foxopen.fde.model.AbstractModelObject;
import net.foxopen.fde.model.Directory;
import net.foxopen.fde.model.FoxModule;
import net.foxopen.fde.model.tree.TreeContentProvider.ITreeNode;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.core.databinding.beans.BeanProperties;

public class FDEMainWindow extends ApplicationWindow {
  private Action action_exit;

  //private static List<AbstractModelObject> d_modules = new ArrayList<AbstractModelObject>();
  private static Directory root = new Directory();
  private StyledText text_documentation;
  private StyledText text_code;
  private TreeViewer treeViewer;
  private Table table;
  private TableViewer tableViewer;
  private Action action_refresh;
  private Action action_open;

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
        composite.setLayout(new FillLayout(SWT.HORIZONTAL));
        {
          tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
          table = tableViewer.getTable();
          table.setLinesVisible(true);
        }
      }
      {
        CTabFolder tabFolder = new CTabFolder(sashForm, SWT.BORDER | SWT.CLOSE);
        tabFolder.setSimple(false);
        tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
        {
          CTabItem tbtmTab = new CTabItem(tabFolder, SWT.CLOSE);
          tbtmTab.setText("Tab");
          {
            Composite composite = new Composite(tabFolder, SWT.NONE);
            tbtmTab.setControl(composite);
            composite.setLayout(new FillLayout(SWT.HORIZONTAL));
            {
              SashForm sashForm_1 = new SashForm(composite, SWT.NONE);

              treeViewer = new TreeViewer(sashForm_1, SWT.BORDER);
              treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
              Tree tree = treeViewer.getTree();
              {
                Composite composite_1 = new Composite(sashForm_1, SWT.NONE);
                composite_1.setLayout(new FillLayout(SWT.VERTICAL));

                Group grpDocumentation = new Group(composite_1, SWT.NONE);
                grpDocumentation.setText("Documentation");
                grpDocumentation.setLayout(new FillLayout(SWT.HORIZONTAL));

                text_documentation = new StyledText(grpDocumentation, SWT.BORDER);

                Group grpCode = new Group(composite_1, SWT.NONE);
                grpCode.setText("Code");
                grpCode.setLayout(new FillLayout(SWT.HORIZONTAL));

                text_code = new StyledText(grpCode, SWT.BORDER);
              }
              sashForm_1.setWeights(new int[] { 1, 3 });
            }
          }
        }
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
          // FIXME Change path
          String path = "\\\\central.health\\dfsuserenv\\users\\User_07\\putalp\\Desktop\\Pierre\\PharmCIS\\CodeSource\\FoxModules\\CoreModules\\PharmCIS";
          //String path = "\\\\central.health\\dfsuserenv\\users\\User_07\\putalp\\Desktop\\Pierre\\PharmCIS";
          
          getStatusLineManager().setMessage("Loading " + path);
          Directory.Load(root, path);
        }
      };
      action_refresh.setAccelerator(SWT.F5);
      action_refresh.setImageDescriptor(ResourceManager.getImageDescriptor(FDEMainWindow.class, "/img/actions/start.png"));
    }
    {
      action_open = new Action("&Open") {

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
    newShell.setText("Fox Documentation Editor - Dev 0.1");
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
    ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
    IObservableMap observeMap = BeansObservables.observeMap(listContentProvider.getKnownElements(), AbstractModelObject.class, "name");
    tableViewer.setLabelProvider(new ObservableMapLabelProvider(observeMap));
    tableViewer.setContentProvider(listContentProvider);
    //
    IObservableList childrenRootObserveList = BeanProperties.list("children").observe(root);
    tableViewer.setInput(childrenRootObserveList);
    //
    return bindingContext;
  }
}
