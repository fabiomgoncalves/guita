

import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;

public class TabItemHandler implements SWTWidgetHandler<TabItem>{

	@Override
	public String getText(TabItem widget) {
		return widget.getText();
	}
	
	@Override
	public Widget getParent(TabItem item) {		
		return item.getParent();
	}

	@Override
	public Widget whenClicked(TabItem item) {
		return item;
	}
}
