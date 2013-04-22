package tinyviewer.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import crossbase.Application;
import crossbase.ui.AboutBox;
import crossbase.ui.DocumentWindowsManager;
import crossbase.ui.MenuConstructor;

public class TinyViewerApplication extends Application
{
	private AboutBox aboutBox = null;
	
	private SelectionAdapter openSelectionAdapter = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(SelectionEvent arg0)
		{
			Shell dummyShell = new Shell(Display.getDefault());
			FileDialog fileDialog = new FileDialog(dummyShell, SWT.OPEN);
			fileDialog.setText("Open image");
			fileDialog.setFilterNames(new String[] { "Image (*.png; *.bmp; *.jpg; *.jpeg)", "All files" });
			fileDialog.setFilterExtensions(new String[] { "*.png; *.bmp; *.jpg; *.jpeg", "*.*" });
			String fileName = fileDialog.open();
			if (fileName != null)
			{
				getDocumentWindowsManager().openFile(fileName);
			}
			dummyShell.dispose();
		}
	};

	public TinyViewerApplication(String[] arguments)
	{
		super(arguments);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected DocumentWindowsManager<ViewWindow> prepareDocumentWindowsManager()
	{
		return new DocumentWindowsManager<ViewWindow>(new ViewWindowFactory(getMenuConstructor()));
	}
	
	@Override
	protected MenuConstructor prepareMenuConstructor()
	{
		TinyViewerMenuConstructor menuConstructor = new TinyViewerMenuConstructor();
		menuConstructor.setOpenSelectionAdapter(openSelectionAdapter);
		return menuConstructor;
	}

}