


public interface AbstractWidgetHandler<W,R> {

	String getText(W widget);
	
	R getParent(W widget);
	
	R whenClicked(W widget);
	
	
	
}
