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

	public ViewTable(){
		instance = this;
	}

	public static ViewTable getInstance(){
		return instance;
	}

	public void addWidget(String name, String type, String localization){
		widgets.add(new WidgetReference(name, type, localization));
		viewer.refresh();
	}
	
	public List<WidgetReference> getWidgetsList(){
		return widgets;
	}
	
	public void refresh(){
		viewer.refresh();
	}


	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			WidgetReference p = (WidgetReference) element;

			if(columnIndex == 0)
				return p.getName();
			else if(columnIndex == 1)
				return p.getType();
			else
				return p.getLocalization();
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
		String[] titles = { "Name", "Type", "Localization"};
		int[] bounds = { 100, 100, 100};

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