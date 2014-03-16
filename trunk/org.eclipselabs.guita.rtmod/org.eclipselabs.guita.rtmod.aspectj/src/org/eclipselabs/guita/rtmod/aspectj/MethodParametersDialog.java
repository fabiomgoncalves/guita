package teste;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class MethodParametersDialog {
	private MethodAbstract methodAbstract;
	private Shell shell;
	private Display display;	
	private LinkedList<String> vals = new LinkedList<String>();
	
	public MethodParametersDialog (MethodAbstract methodAbstract) {
		this.methodAbstract  = methodAbstract;
		display = Display.getDefault();
		shell = new Shell(display, SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		shell.setLayout(new FillLayout(SWT.VERTICAL));
	}	
	
	public LinkedList<String> open () {
		int width = 200;
		int height = 30;

		for (String parameterType : methodAbstract.getParameterTypes()) {
			Label lblNewLabel = new Label(shell, SWT.NONE);
			lblNewLabel.setText(parameterType);
			Text text = new Text(shell, SWT.BORDER);
			text.setText("");
			height += 70;
		}

		Button b = new Button(shell, SWT.NONE);
		b.setText("Novo Texto");
		b.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (Control c : shell.getChildren()) {
					if (c.getClass().getName().equals("org.eclipse.swt.widgets.Text")) {
						vals.add(((Text) c).getText());
					}
				}
				shell.dispose();
			}
		});

		shell.setSize(width, height);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return vals;
	}
}