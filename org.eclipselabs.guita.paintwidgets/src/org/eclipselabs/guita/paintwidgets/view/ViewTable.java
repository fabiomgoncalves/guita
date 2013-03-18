package org.eclipselabs.guita.paintwidgets.view;


import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

public class ViewTable extends ViewPart{

	private TableViewer viewer;
	private Table table;

	
//	private static class ContentProvider implements IStructuredContentProvider {
//		public Object[] getElements(Object inputElement) {
//			
//		}
//		public void dispose() {
//		}
//		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//		}
//	}

	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			WidgetReference p = (WidgetReference) element;
			String result = "";
			switch(columnIndex){
			case 0:
				result = p.getName();
				break;
			case 1:
				result = p.getType();
				break;
			case 2:
				result = p.getLocalization();
				break;
			default:
				result = "";
			}
			return result;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);
		table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		//viewer.setLabelProvider(new TableLabelProvider());
		//viewer.setContentProvider(new ContentProvider());


		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		//gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "Name", "Type", "Localization"};
		int[] bounds = { 100, 100, 100};


		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				WidgetReference p = (WidgetReference) element;
				return p.getName();
			}
		});

		col = createTableViewerColumn(titles[1], bounds[1]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				WidgetReference p = (WidgetReference) element;
				return p.getType();
			}
		});

		col = createTableViewerColumn(titles[2], bounds[2]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				WidgetReference p = (WidgetReference) element;
				return p.getLocalization();
			}
		});

		//		col = createTableViewerColumn(titles[3], bounds[3]);
		//		col.setLabelProvider(new ColumnLabelProvider() {
		//			@Override
		//			public String getText(Object element) {
		//				return null;
		//			}
		//
		//			@Override
		//			public Image getImage(Object element) {
		//				return img;
		//			}
		//		});
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