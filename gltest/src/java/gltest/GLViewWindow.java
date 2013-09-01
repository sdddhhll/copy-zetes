package gltest;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.CrossBaseGLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import crossbase.ui.ViewWindowBase;

public class GLViewWindow extends ViewWindowBase<GLDocument>
{
	// Model constants
	private static final int MODEL_CUBE = 0;
	private static final int MODEL_MONKEY_SIMPLE = 1;
	private static final int MODEL_MONKEY_SUBDIVIDED = 2;
	
	private static native boolean globalInit();
	private static native boolean createScene(int model, int width, int height);
	private static native boolean destroyScene();
	private static native boolean resizeView(int width, int height);
	private static native boolean drawScene(double angle);
	
	private double angle = 0;
	private CrossBaseGLCanvas canvas;
	private Date lastFrameMoment = new Date();
	private float framesPerSecond = 50;
	
	public GLViewWindow(String applicationTitle)
	{
		super(applicationTitle);
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	protected Shell constructShell()
	{
		Shell shell = new Shell(SWT.CLOSE | SWT.MIN | SWT.TITLE | SWT.MAX | SWT.RESIZE | SWT.DOUBLE_BUFFERED);
		
		Point size = shell.getSize();
		Point clientSize = new Point(shell.getClientArea().width, shell.getClientArea().height);
		
		shell.setSize(size.x - clientSize.x + 800, size.y - clientSize.y + 600);
		if (SWT.getPlatform().equals("win32"))
		{
			shell.setImage(SWTResourceManager.getImage(GLViewWindow.class, "/gltest/gltest16.png"));
		}
		else
		{
			shell.setImage(SWTResourceManager.getImage(GLViewWindow.class, "/gltest/gltest64.png"));
		}
		
		shell.setLayout(new FillLayout());
		Composite comp = new Composite(shell, SWT.NO_BACKGROUND);
		comp.setLayout(new FillLayout());
		GLData data = new GLData ();
		data.doubleBuffer = true;
		data.depthSize = 1;
		data.samples = 1;
		data.sampleBuffers = 1;
		
		canvas = new CrossBaseGLCanvas(comp, SWT.NO_BACKGROUND, data);
		
		if (!canvas.isCurrent()) canvas.setCurrent();
		globalInit();
		
		Point csize = canvas.getSize();
		createScene(MODEL_CUBE, csize.x, csize.y);
		
		canvas.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0)
			{
				Point size = canvas.getSize();
				
				if (arg0.character == '1')
				{
					destroyScene();
					createScene(MODEL_CUBE, size.x, size.y);
				}
				else if (arg0.character == '2')
				{
					destroyScene();
					createScene(MODEL_MONKEY_SIMPLE, size.x, size.y);
				}
				else if (arg0.character == '3')
				{
					destroyScene();
					createScene(MODEL_MONKEY_SUBDIVIDED, size.x, size.y);
				}
			}
		});
		
		canvas.addControlListener(new ControlListener()
		{
			
			@Override
			public void controlResized(ControlEvent arg0)
			{
				if (!canvas.isCurrent()) canvas.setCurrent();
				Point size = canvas.getSize();
				resizeView(size.x, size.y);
			}
			
			@Override
			public void controlMoved(ControlEvent arg0)
			{
			}
		});
		
		canvas.addPaintListener(new PaintListener()
		{
			@Override
			public void paintControl(PaintEvent arg0)
			{
				if (!canvas.isCurrent()) canvas.setCurrent();
				drawScene(angle);
				canvas.swapBuffers();
			}
		});

		return shell;
	}
	
	public void updateFrame()
	{
		Date currentMoment;
		double deltaTimeSec;
		
		do
		{
			currentMoment = new Date();
			deltaTimeSec = 0.001 * (currentMoment.getTime() - lastFrameMoment.getTime());
			try {
				Thread.sleep(Math.max(1, (long) (1000 / framesPerSecond)));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while (deltaTimeSec < 1.0 / framesPerSecond);
			
		{
			angle += 0.5 * deltaTimeSec;
			if (canvas != null && !canvas.isDisposed())
			{
				canvas.redraw();
			}
			lastFrameMoment = currentMoment;
		}
	}
	
	@Override
	public boolean supportsFullscreen()
	{
		return true;
	}
	
	@Override
	public boolean supportsMaximizing()
	{
		return true;
	}
}
