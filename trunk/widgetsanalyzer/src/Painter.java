import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;


public class Painter {

	private Color oldState;

	public void paint(Widget g){
		Control c = (Control)g;
		oldState = c.getBackground();
		c.setBackground(new Color(null, 0, 0, 0));
	}

	public void dispaint(Widget g){
		Control c = (Control)g;
		c.setBackground(oldState);
	}

}