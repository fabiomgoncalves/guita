package org.eclipselabs.guita.paintwidgets.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ViewTable extends ViewPart{

	public static final String ID = "org.eclipselabs.guita.paintwidgets.ViewTable";

	private static ViewTable instance;

	private TableViewer viewer;
	private List<WidgetTable> widgets = new ArrayList<WidgetTable>();


	public ViewTable(){
		instance = this;
	}

	public static ViewTable getInstance() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {			
			page.showView(ID);			
		} 
		catch (PartInitException e) {
			e.printStackTrace();
		}	
		return instance;
	}
	
	public IStructuredSelection getSelection() {
		return (IStructuredSelection) viewer.getSelection();
	}

	public void addWidget(String name, String type, String localization, String color){
		WidgetTable aux = new WidgetTable(name, type, localization, color);
		boolean found = false;

		if(!widgets.isEmpty()){
			for(WidgetTable w: widgets){
				if(w.getName().equals(aux.getName())){
					if(!w.getColor().equals(aux.getColor())){
						w.setColor(aux.getColor());
						found = true;
					}
				}
			}
		}
		if(widgets.isEmpty() || !found){
			widgets.add(aux);
		}

		viewer.refresh();
	}

	public void removeWidget(WidgetTable g){
		widgets.remove(g);
		viewer.refresh();
	}

	public boolean alreadyPainted(String name, String type, String localization, String color){
		WidgetTable aux = new WidgetTable(name, type, localization, color);

		if(!widgets.isEmpty()){
			for(WidgetTable w: widgets){
				if(w.getName().equals(aux.getName())){
					return true;
				}
			}
		}
		return false;
	}

	public void refresh(){
		viewer.refresh();
	}


	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			WidgetTable w = (WidgetTable) element;
			String result = "";
			switch(columnIndex){
			case 0:
				result = w.getName();
				break;
			case 1:
				result = w.getType();
				break;
			case 2:
				result = w.getLocation();
				break;
			case 3:
				break;
			default:
				result = "";
			}
			return result;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			Image image = null;
			if(columnIndex == 3){
				WidgetTable w = (WidgetTable) element;
				
				PaletteData paletteData = new PaletteData(new RGB[] {w.getColorRGB()});
				ImageData imageData = new ImageData(70,25,1,paletteData);
				image = new Image(null,imageData);
			}
			return image;
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
		String[] titles = {"Name", "Type", "Location", "Color"};
		int[] bounds = { 150, 200, 120, 100};

		for(int i = 0; i != titles.length; i++){
			createTableViewerColumn(titles[i], bounds[i]);
		}
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(false);
		column.setMoveable(true);
		return viewerColumn;
	}

	@Override
	public void setFocus() {
	}

}