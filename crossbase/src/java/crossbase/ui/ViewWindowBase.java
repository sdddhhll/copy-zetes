package crossbase.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import crossbase.abstracts.Document;
import crossbase.abstracts.ViewWindow;

public abstract class ViewWindowBase<TD extends Document> implements ViewWindow<TD>
{
	private Shell shell;
	private String applicationTitle;
	private TD document;
	
	protected Shell getShell() {
		return shell;
	}
	
	public boolean isDisposed() {
		return shell.isDisposed();
	}
	
	@Override
	public TD getDocument()
	{
		return document;
	}
	
	@Override
	public void addShellListener(ShellListener shellListener) {
		shell.addShellListener(shellListener);
	}
	
	@Override
	public String getTitle() {
		return shell.getText();
	}
	
	@Override
	public void setDocument(TD document)
	{
		this.document = document;
	}
	
	@SuppressWarnings("rawtypes")
	private void setCocoaFullscreenButton(boolean on)
	{
		try
		{
			Field field = Control.class.getDeclaredField("view");
			Object /*NSView*/ view = field.get(shell);
	
			if (view != null)
			{
			    Class<?> c = Class.forName("org.eclipse.swt.internal.cocoa.NSView");
			    Object /*NSWindow*/ window = c.getDeclaredMethod("window").invoke(view);
	
			    c = Class.forName("org.eclipse.swt.internal.cocoa.NSWindow");
			    Method setCollectionBehavior = c.getDeclaredMethod(
			        "setCollectionBehavior", long.class);
			    setCollectionBehavior.invoke(window, on ? (1 << 7) : 0);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public ViewWindowBase(String applicationTitle)
	{
		this.applicationTitle = applicationTitle;
	}
	
	@Override
	public final Menu getMenu()
	{
		return shell.getMenuBar();
	}
	
	@Override
	public final void open()
	{
		shell = constructShell();
		prepareShell();

		shell.layout();
		shell.open();
	}
	
	protected abstract Shell constructShell();
	
	@Override
	public final boolean isActive()
	{
		if (Display.getCurrent() != null && !Display.getCurrent().isDisposed())
		{
			return Display.getCurrent().getActiveShell() == shell;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public final void activate(boolean force) 
	{
		shell.setMinimized(false);
		if (force)
		{
			shell.forceActive();
		}
		else
		{
			shell.setActive();
		}
	}
	
	@Override
	public final void toggleMinimized()
	{
		shell.setMinimized(!shell.getMinimized());
	}
	
	@Override
	public final void toggleMaximized()
	{
		if (shell.getFullScreen()) shell.setFullScreen(false);
		shell.setMaximized(!shell.getMaximized());
	}
	
	@Override
	public final void toggleFullScreen()
	{
		if (shell.getMaximized()) shell.setMaximized(false);
		shell.setFullScreen(!shell.getFullScreen());
	}

	public void widgetDisposed(DisposeEvent arg0)
	{
		// If we want this to work, we should guarantee that the generic parameter type TVW of menuConstructor equals to our type
		 
		
		Document doc = getDocument();
		if (doc != null) 
		{
			doc.dispose();
		}
	}
	
	private void prepareShell()
	{
		shell.setText(applicationTitle);

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		if (SWT.getPlatform().equals("cocoa"))
		{
			setCocoaFullscreenButton(supportsFullscreen());
		}
	}

	public String getApplicationTitle()
	{
		return applicationTitle;
	}
}
