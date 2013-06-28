import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SWTDrawOval {

	public static void main (String [] args) {
		final Display display = new Display ();
		final Shell shell = new Shell(display);
		shell.setLayout(new RowLayout());

		final PaintListener listener = new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setForeground(new Color(display, 255,0,0));
				e.gc.drawOval(0, 0, e.width-1, e.height-1);
			}
		}; 

		final Button b = new Button(shell, SWT.PUSH);
		b.setText("OK");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				b.addPaintListener(listener);
				b.redraw();
			}
		});


		final Button rem = new Button(shell, SWT.PUSH);
		rem.setText("remove");
		rem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				b.removePaintListener(listener);
				b.redraw();
			}
		});


		shell.pack();
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
}