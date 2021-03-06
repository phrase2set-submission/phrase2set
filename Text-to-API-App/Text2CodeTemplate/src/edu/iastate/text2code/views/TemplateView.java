package edu.iastate.text2code.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import edu.iastate.text2code.Activator;
import edu.iastate.text2code.utils.ASTUtils;
import edu.iastate.text2code.utils.CodeTemplateRewriter;
import edu.iastate.text2code.utils.FileIO;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class TemplateView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.iastate.text2code.views.TemplateView";
	private final String DATA_PATH = "/Users/thanhnguyen/Workspace/Java/Text2CodeTemplate/data/";
	private TableViewer viewer;
	private Action queryAction;
	private Action doubleClickAction;
	private Display imgDisplay;
	private Label label;
	
	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return new String[] { /*"One", "Two", "Three"*/ };
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public TemplateView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		// Initialize label to display the graph snapshot
		label = new Label(parent, SWT.NONE);
//		label.setText("API Usage Graph");
		makeActions();
		contributeToActionBars();
	}
	
	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "Text2CodeTemplate.viewer");
		hookContextMenu();
		hookDoubleClickAction();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TemplateView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	 // triangle pull-down
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(queryAction);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) { // context menu on view part
		manager.add(queryAction);
		// Other plug-ins can contribute these actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(queryAction);
	}

	private void makeActions() {
		queryAction = new Action() {
			public void run() {
				String query = showQueryDialog();
				if(query.isEmpty())
					return;
				String code = "";
				ImageData data = null;
				if(query.contains("Read") || query.contains("file")) {
					code = FileIO.readFileToString(DATA_PATH + "9292594_code.txt");
					data = new ImageData(DATA_PATH + "9292594_graph.gif");
				}
				else if(query.contains("GPS") || query.contains("location")) {
					code = FileIO.readFileToString(DATA_PATH + "13761430_code.txt");
					data = new ImageData(DATA_PATH + "13761430_graph.gif");
				}
				if(data != null) {
					generateTemplate(query, code);
					displaySnapshot(data);
				}
			}
		};
		queryAction.setText("Action");
		queryAction.setToolTipText("Query API usage");
		queryAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(), "Template View", message);
	}
	
	private String showQueryDialog() {
		InputDialog d = new InputDialog(/*viewer.getControl().getShell()*/label.getShell(), "Input Query", 
				"Describe your programming task here", "", null);
		d.open();
		String v = d.getValue();
		return v;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		label.setFocus();
	}
	
	private void generateTemplate(String query, String code) {
		int index = ASTUtils.getCurrentCursor();
		CodeTemplateRewriter.insertComments(query, code, index);
	}
	
	private void displaySnapshot(ImageData imgData) {
		// create a display to store image
		Image snapshot = new Image(imgDisplay, imgData);
		label.setImage(snapshot);
		System.out.println(snapshot.getImageData().width + "x" + snapshot.getImageData().height);
	}
}