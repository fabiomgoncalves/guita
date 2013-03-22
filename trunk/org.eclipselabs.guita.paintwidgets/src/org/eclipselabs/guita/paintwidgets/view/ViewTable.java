package org.eclipselabs.guita.paintwidgets.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

public class ViewTable extends ViewPart{

	public static final String ID = "org.eclipselabs.guita.paintwidgets.ViewTable";

	private static ViewTable instance;

	private TableViewer viewer;
	private List<WidgetReference> widgets = new ArrayList<WidgetReference>();

	private static final Image redImage = Activator.getImageDescriptor("Icons/Red.png").createImage();
	private static final Image blueImage = Activator.getImageDescriptor("Icons/Blue.png").createImage();
	private static final Image greenImage = Activator.getImageDescriptor("Icons/Green.png").createImage();
	private static final Image yellowImage = Activator.getImageDescriptor("Icons/Yellow.jpg").createImage();
	private static final Image pinkImage = Activator.getImageDescriptor("Icons/Pink.jpg").createImage();


	public ViewTable(){
		instance = this;
	}

	public static ViewTable getInstance(){
		return instance;
	}

	public void addWidget(String name, String type, String localization, String color){
		widgets.add(new WidgetReference(name, type, localization, color));
		viewer.refresh();
	}

	public List<WidgetReference> getWidgetsList(){
		return widgets;
	}

	public void refresh(){
		viewer.refresh();
	}


	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			WidgetReference w = (WidgetReference) element;
			String result = "";
			switch(columnIndex){
			case 0:
				result = w.getName();
				break;
			case 1:
				result = w.getType();
				break;
			case 2:
				result = w.getLocalization();
				break;
			case 3:
				break;
			default:
				result = "";
			}
			return result;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			if(columnIndex == 3){
				WidgetReference w = (WidgetReference) element;
				if(w.getColor().equals("Red")){
					return redImage;
				}
				if(w.getColor().equals("Blue")){
					return blueImage;
				}
				if(w.getColor().equals("Green")){
					return greenImage;
				}
				if(w.getColor().equals("Yellow")){
					return yellowImage;
				}
				if(w.getColor().equals("Pink")){
					return pinkImage;
				}
			}
			return null;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setLabelProvider(new TableLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());

		viewer.setInput(widgets);
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "Name", "Type", "Localization", "Color"};
		int[] bounds = { 100, 100, 100, 100};

		for(int i = 0; i != titles.length; i++){
			createTableViewerColumn(titles[i], bounds[i]);
		}
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	@Override
	public void setFocus() {
	}

}