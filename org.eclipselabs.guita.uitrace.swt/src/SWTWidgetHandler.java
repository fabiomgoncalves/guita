

import org.eclipse.swt.widgets.Widget;

public interface SWTWidgetHandler<W extends Widget> extends AbstractWidgetHandler<W, Widget>{

	Widget getParent(W widget);
	
	Widget whenClicked(W widget);
	
}
