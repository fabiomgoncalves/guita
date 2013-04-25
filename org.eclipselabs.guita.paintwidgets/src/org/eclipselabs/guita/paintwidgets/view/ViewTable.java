package org.eclipselabs.guita.paintwidgets.view;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.eclipselabs.guita.request.Request;
import org.eclipselabs.guita.request.Request.Color;

public class ViewTable extends ViewPart{

	public static final String ID = "org.eclipselabs.guita.paintwidgets.ViewTable";

	public static final int PORT_IN = 8080;
	private ServerSocket serverSocket;
	private Socket socket = null;
	private ObjectOutputStream oos = null;

	private static ViewTable instance;

	private TableViewer viewer;
	private List<TableWidgetReference> widgets = new ArrayList<TableWidgetReference>();


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

	public boolean alreadyPainted(TableWidgetReference aux){
		if(!widgets.isEmpty()){
			for(TableWidgetReference w: widgets){
				if(w.getName().equals(aux.getName()) && w.getLocation().equals(aux.getLocation())){
					return true;
				}
			}
		}
		return false;
	}

	public void addWidget(TableWidgetReference newWidget){
		boolean found = false;

		if(!widgets.isEmpty()){
			for(TableWidgetReference w: widgets){
				if(w.getName().equals(newWidget.getName()) && w.getLocation().equals(newWidget.getLocation())){
					w.setColor(newWidget.getColor());
					found = true;
				}
			}
		}
		if(!found){
			widgets.add(newWidget);
		}

		viewer.refresh();
	}

	public void removeWidget(TableWidgetReference g){
		widgets.remove(g);
		viewer.refresh();
	}

	public void sendWidgetsList(){

		try {
			serverSocket = new ServerSocket(PORT_IN);
		} catch (IOException e) {
			System.exit(1);
		}

		while(true){
			List<Request> pendingRequests = new ArrayList<Request>();
			try {
				socket = serverSocket.accept();
				oos = new ObjectOutputStream(socket.getOutputStream());
				
				Iterator<TableWidgetReference> iterator = widgets.iterator();
				while(iterator.hasNext()){
					TableWidgetReference g = iterator.next();
					Request request = Request.newPaintRequest(g.getLocation(), mapColor(g.getColor()));

					pendingRequests.add(request);
				}

				oos.writeObject(pendingRequests);
			}
			catch(Exception e) {
				e.printStackTrace();
			} finally {
				if(oos != null){
					try {
						oos.close(); } catch(IOException e){}
				}
				if(socket != null){
					try {
						socket.close(); } catch(IOException e){}
				}
			}
		}
	}

	public Color mapColor(String color){
		if(color.equals("Red")){
			return Color.RED;
		}else if(color.equals("Blue")){
			return Color.BLUE;
		}else if(color.equals("Green")){
			return Color.GREEN;
		}else if(color.equals("Yellow")){
			return Color.YELLOW;
		}else{
			return Color.PINK;
		}
	}

	public void refresh(){
		viewer.refresh();
	}


	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			TableWidgetReference w = (TableWidgetReference) element;
			String result = "";
			switch(columnIndex){
			case 0: //primeira coluna em branco devido a um bug do SWT
				break;
			case 1:
				result = w.getName();
				break;
			case 2:
				result = w.getType();
				break;
			case 3:
				result = w.getLocation();
				break;
			case 4:
				break;
			default:
				result = "";
			}
			return result;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			Image image = null;
			if(columnIndex == 4){
				TableWidgetReference w = (TableWidgetReference) element;

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
		String[] titles = {"", "Name", "Type", "Location", "Color"}; //primeira coluna em branco devido a um bug do SWT
		int[] bounds = { 0, 150, 200, 120, 100};

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