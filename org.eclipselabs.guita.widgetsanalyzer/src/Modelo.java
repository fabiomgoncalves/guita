
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Modelo {
	
	private static Button[] ola = new Button[10];

	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Form 1");

		Label label1 = new Label(shell, SWT.NULL);
		label1.setText("Algum texto !!!");
		
		for(int i = 0; i != ola.length; i++){
			ola[i] = new Button(shell, SWT.PUSH);
			ola[i].setText("ALGO");
		}

		Button button1 = new Button(shell, SWT.PUSH);
		button1.setText("OK");

		Button button2 = new Button(shell, SWT.PUSH);
		button2.setText("Cancelar");

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