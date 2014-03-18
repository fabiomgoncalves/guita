package teste;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class MethodDialog {
	private MethodContainer method;
	private Shell shell;
	private Display display;	
	private LinkedList<Object> vals;

	public MethodDialog (MethodContainer method) {
		this.method  = method;
		display = Display.getDefault();
		shell = new Shell(display, SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		shell.setLayout(new FillLayout(SWT.VERTICAL));
	}	

	public Object[] open () {
		this.vals = new LinkedList<Object>();
		final LinkedList<String> cc = new LinkedList<String>();

		int width = 200;
		int height = 30;

		for (ParameterContainer parameter : method.getParameters()) {
			Label lblNewLabel = new Label(shell, SWT.NONE);
			lblNewLabel.setText(parameter.getType() + " " + parameter.getName());
			cc.add(parameter.getType());
			Text text = new Text(shell, SWT.BORDER);
			text.setText("");
			height += 70;
		}

		Button b = new Button(shell, SWT.NONE);
		b.setText("OK");
		b.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = 0;
				for (Control c : shell.getChildren()) {
					if (c.getClass().getName().equals("org.eclipse.swt.widgets.Text")) {	
						Object o = createNewObject(cc.get(i), ((Text) c).getText());
						vals.add(o);
						i++;
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

		return vals.toArray(new Object[vals.size()]);
	}

	private Object createNewObject(String _class, String value) {
		switch (_class) {
		case "int":
		case "Integer":
			return Integer.parseInt(value);
		case "String":
			return value;
		case "boolean":
		case "Boolean":
			return Boolean.valueOf(value);
		case "Character":
		case "char":
			return value.charAt(0);
		case "Short":
		case "short":
			return Short.parseShort(value);
		case "Long":
		case "long":
			return Long.parseLong(value);
		case "Float":
		case "float":
			return Float.parseFloat(value);
		case "Double":
		case "double":
			return Double.parseDouble(value);
		}
		return null;
	}
}