package org.eclipselabs.guita.codetrace.plugin.view;

import java.io.IOException;
import java.io.ObjectInputStream;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipselabs.guita.codetrace.common.Request;
import org.eclipselabs.guita.codetrace.common.Request.Color;

public class ViewTable extends ViewPart{

	private static final String ID = "org.eclipselabs.guita.paintwidgets.ViewTable";

	private static final int PORT2 = 8090;
	private static final int PORT3 = 8100;
	private ServerSocket serverSocket2;
	private ServerSocket serverSocket3;

	private static ViewTable instance;

	private TableViewer viewer;
	private List<TableWidgetReference> widgets = new ArrayList<TableWidgetReference>();


	public ViewTable(){
		instance = this;

		try {
			serverSocket2 = new ServerSocket(PORT2);
			serverSocket3 = new ServerSocket(PORT3);
		} catch (IOException e) {
			System.exit(1);
		}

		new InitializationService().start();
		new ReceivePaintedWidgetsNumber().start();
	}

	public class InitializationService extends Thread {
		@Override
		public void run() {
			Socket socket = null;
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			List<Request> pendingRequests = null;

			while(true){
				pendingRequests = new ArrayList<Request>();
				try {
					socket = serverSocket3.accept();
					ois = new ObjectInputStream(socket.getInputStream());
					@SuppressWarnings("unused")
					String status = (String) ois.readObject();

					Iterator<TableWidgetReference> iterator = widgets.iterator();
					while(iterator.hasNext()){
						TableWidgetReference g = iterator.next();
						Request request = Request.newPaintRequest(g.getLocation(), g.getInfo(), mapColor(g.getColor()), g.getName());
						pendingRequests.add(request);
					}
					oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(pendingRequests);

				}
				catch(Exception e) {
					e.printStackTrace();
				} finally {
					if(oos != null){
						try { oos.close(); } catch(IOException e){}
					}
					if(socket != null){
						try { socket.close(); } catch(IOException e){}
					}
				}
			}
		}
	}

	public class ReceivePaintedWidgetsNumber extends Thread {
		@Override
		public void run() {
			Socket socket = null;
			ObjectInputStream ois = null;

			while(true){
				try {
					socket = serverSocket2.accept();
					ois = new ObjectInputStream(socket.getInputStream());
					int number = (Integer) ois.readObject();
					String location = (String) ois.readObject();
					String type = (String) ois.readObject();
					String name = (String) ois.readObject();

					for(TableWidgetReference w: widgets){
						if(w.getLocation().equals(location) && w.getType().equals(type) && w.getName().equals(name))
							w.setNumberPaintedWidgets(number);
					}

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							refresh();
						}
					});

				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
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

	public boolean alreadyPainted(String name, String location){
		if(!widgets.isEmpty()){
			for(TableWidgetReference w: widgets){
				if(w.getName().equals(name))
					return true;
			}
		}
		return false;
	}

	public void addWidget(TableWidgetReference newWidget){
		boolean found = false;

		if(!widgets.isEmpty()){
			for(TableWidgetReference w: widgets){
				if(w.getName().equals(newWidget.getName())){
					w.setColor(newWidget.getColor());
					found = true;
				}
			}
		}
		if(!found)
			widgets.add(newWidget);

		viewer.refresh();
	}

	public void removeWidget(TableWidgetReference g){
		widgets.remove(g);
		viewer.refresh();
	}

	public List<TableWidgetReference> getWidgetsTable(){
		return widgets;
	}
	
	public void clearAll() {
		widgets.clear();
		viewer.refresh();
	}

	public Color mapColor(String color){
		if(color.equals("Red"))
			return Color.RED;
		else if(color.equals("Blue"))
			return Color.BLUE;
		else if(color.equals("Green"))
			return Color.GREEN;
		else if(color.equals("Yellow"))
			return Color.YELLOW;
		else if(color.equals("Pink"))
			return Color.PINK;
		else
			return Color.WHITE;
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
				boolean end = false;
				int i = 0;
				while(!end){
					result += w.getLocation().charAt(i);

					if(w.getLocation().charAt(++i) == ':'){
						end = true;
					}
				}

				break;
			case 4:
				break;
			case 5:
				result = Integer.toString(w.getNumberOfPaintedWidgets());
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
		String[] titles = {"", "Variable", "Type", "Location", "Color", "Widgets"}; //primeira coluna em branco devido a um bug do SWT
		int[] bounds = { 0, 150, 150, 200, 100, 250};

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