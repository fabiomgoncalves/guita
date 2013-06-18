package org.eclipselabs.guita.widgetsanalyzer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Modelo {
	
	private static Button[] array = new Button[10];
	
	private static Shell shell;

	public static void main(String[] args) {
		final Display display = new Display();
		shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Form 1");

		final Label label1 = new Label(shell, SWT.NULL);
		label1.setText("Algum texto !!!");
		
		for(int i = 0; i != array.length; i++){
			Button b = new Button(shell, SWT.PUSH);
			array[i] = b;
			array[i].setText("ALGO");
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					label1.setText(label1.getText() + "!");
					label1.getParent().layout();
				}
			});
		}
		
		
		Button button1 = new Button(shell, SWT.PUSH);
		button1.setText("OK");
		button1.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				shell.setVisible(false);
				final Shell shell2 = new Shell(display);
				shell2.setLayout(new GridLayout(1, false));
				shell2.setText("Form 2");shell2.setText(shell.getText() + "?");shell2.setText(shell2.getText());
				
				Label label1 = new Label(shell2, SWT.NULL);
				label1.setText("DOIS");
				
				Button button2 = new Button(shell2, SWT.PUSH);
				button2.setText("TRES");
				button2.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e){
						shell2.dispose();
						shell.setVisible(true);
					}
				});
				
				shell2.pack ();
				shell2.open ();
				while (!shell2.isDisposed ()) {
					if (!display.readAndDispatch ()) 
						display.sleep ();
				}
			}
		});

		Button button2 = new Button(shell, SWT.PUSH);
		button2.setText(label1.getText());

		Text text = new Text(shell, SWT.SINGLE);
		text.setText("Introduzir algum texto aqui...");

		label1.setText("Outro texto");

		shell.pack ();
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) 
				display.sleep ();
		}
		display.dispose ();
	}

}