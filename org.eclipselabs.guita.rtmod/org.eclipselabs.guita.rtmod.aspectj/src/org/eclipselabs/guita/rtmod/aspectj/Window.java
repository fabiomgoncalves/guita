package teste;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

public class Window {

	/**
	 * Launch the application.
	 * @param args
	 */

	private Shell shell;
	private Display display;

	public static void main(String args[]) {
		Window main = new Window();
		main.run();
	}

	/**
	 * Create the shell.
	 * @param display
	 */
	public void run() {
		try {
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public Window() {	
		display = Display.getDefault();
		shell = new Shell(display, SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		shell.setSize(248, 104);
	
		Button button = new Button(shell, SWT.NONE);
		button.setBounds(30, 30, 200, 30);		
		button.setText("OK");	
	}
}
